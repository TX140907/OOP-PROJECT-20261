package sanbaka.assets;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Bộ phát video nền MP4 lặp vô hạn (loop) sử dụng Windows Media Foundation thông qua Java FFM API.
 * Tích hợp nội suy song tuyến tính (Bilinear Luma & Chroma Upsampling), chuẩn màu HD BT.709
 * và bộ lọc làm nét thích ứng (Contrast-Adaptive Sharpening) để khử răng cưa/pixel và tăng độ sắc nét.
 */
public final class VideoBackgroundPlayer {
    private static final int FIRST_VIDEO_STREAM = 0xFFFFFFFC;
    private static final int MF_SOURCE_READERF_ERROR = 0x1;
    private static final int MF_SOURCE_READERF_ENDOFSTREAM = 0x2;

    private VideoBackgroundPlayer() {}

    public static boolean startLoopingVideo(
            File videoFile,
            int targetW,
            int targetH,
            BooleanSupplier shouldDecode,
            Consumer<BufferedImage> onFrameReady) {
        if (videoFile == null || !videoFile.isFile()) return false;
        if (!System.getProperty("os.name", "").toLowerCase().contains("win")) return false;

        BufferedImage frontBuffer = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
        BufferedImage backBuffer = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);

        boolean[] firstFrameDecoded = {false};
        Object firstFrameLock = new Object();

        Thread decoderThread = new Thread(() -> {
            Linker linker = Linker.nativeLinker();
            try (Arena arena = Arena.ofConfined()) {
                SymbolLookup ole32 = SymbolLookup.libraryLookup("ole32.dll", arena);
                SymbolLookup mfplat = SymbolLookup.libraryLookup("mfplat.dll", arena);
                SymbolLookup mfrw = SymbolLookup.libraryLookup("mfreadwrite.dll", arena);

                MethodHandle coInit = linker.downcallHandle(ole32.find("CoInitializeEx").orElseThrow(),
                        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
                MethodHandle mfStartup = linker.downcallHandle(mfplat.find("MFStartup").orElseThrow(),
                        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
                MethodHandle mfCreateMediaType = linker.downcallHandle(mfplat.find("MFCreateMediaType").orElseThrow(),
                        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
                MethodHandle mfCreateSourceReader = linker.downcallHandle(mfrw.find("MFCreateSourceReaderFromURL").orElseThrow(),
                        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));

                int hr = (int) coInit.invokeExact(MemorySegment.NULL, 0);
                hr = (int) mfStartup.invokeExact(0x00020070, 0);
                if (hr < 0) return;

                char[] urlChars = (videoFile.getAbsolutePath() + "\0").toCharArray();
                MemorySegment urlSeg = arena.allocate(urlChars.length * 2L);
                for (int i = 0; i < urlChars.length; i++) {
                    urlSeg.set(ValueLayout.JAVA_CHAR_UNALIGNED, i * 2L, urlChars[i]);
                }

                MemorySegment ppReader = arena.allocate(ValueLayout.ADDRESS);
                hr = (int) mfCreateSourceReader.invokeExact(urlSeg, MemorySegment.NULL, ppReader);
                if (hr < 0) return;
                MemorySegment reader = ppReader.get(ValueLayout.ADDRESS, 0);

                MemorySegment ppType = arena.allocate(ValueLayout.ADDRESS);
                hr = (int) mfCreateMediaType.invokeExact(ppType);
                if (hr < 0) return;
                MemorySegment mediaType = ppType.get(ValueLayout.ADDRESS, 0);

                // MF_MT_MAJOR_TYPE = Video, MF_MT_SUBTYPE = NV12
                MemorySegment guidMajor = arena.allocate(16);
                putGuid(guidMajor, 0x48eba18e, (short) 0xf8c9, (short) 0x4687,
                        (byte) 0xbf, (byte) 0x11, (byte) 0x0a, (byte) 0x74, (byte) 0xc9, (byte) 0xf9, (byte) 0x6a, (byte) 0x8f);
                MemorySegment guidVideo = arena.allocate(16);
                putGuid(guidVideo, 0x73646976, (short) 0x0000, (short) 0x0010,
                        (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0xaa, (byte) 0x00, (byte) 0x38, (byte) 0x9b, (byte) 0x71);
                MemorySegment guidSubtype = arena.allocate(16);
                putGuid(guidSubtype, 0xf7e34c9a, (short) 0x42e8, (short) 0x4714,
                        (byte) 0xb7, (byte) 0x4b, (byte) 0xcb, (byte) 0x29, (byte) 0xd7, (byte) 0x2c, (byte) 0x35, (byte) 0xe5);
                MemorySegment guidNV12 = arena.allocate(16);
                putGuid(guidNV12, 0x3231564E, (short) 0x0000, (short) 0x0010,
                        (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0xaa, (byte) 0x00, (byte) 0x38, (byte) 0x9b, (byte) 0x71);

                MethodHandle setGuid = linker.downcallHandle(vtableFunc(mediaType, 24),
                        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
                hr = (int) setGuid.invokeExact(mediaType, guidMajor, guidVideo);
                hr = (int) setGuid.invokeExact(mediaType, guidSubtype, guidNV12);

                MethodHandle setCurrentMediaType = linker.downcallHandle(vtableFunc(reader, 7),
                        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
                hr = (int) setCurrentMediaType.invokeExact(reader, FIRST_VIDEO_STREAM, MemorySegment.NULL, mediaType);
                if (hr < 0) return;

                MemorySegment ppCurType = arena.allocate(ValueLayout.ADDRESS);
                MethodHandle getCurrentMediaType = linker.downcallHandle(vtableFunc(reader, 6),
                        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
                hr = (int) getCurrentMediaType.invokeExact(reader, FIRST_VIDEO_STREAM, ppCurType);
                if (hr < 0) return;
                MemorySegment curType = ppCurType.get(ValueLayout.ADDRESS, 0);

                // Đọc kích thước khung hình (MF_MT_FRAME_SIZE)
                MemorySegment guidFrameSize = arena.allocate(16);
                putGuid(guidFrameSize, 0x1652c33d, (short) 0xd6b2, (short) 0x4012,
                        (byte) 0xb8, (byte) 0x34, (byte) 0x72, (byte) 0x03, (byte) 0x08, (byte) 0x49, (byte) 0xa3, (byte) 0x7d);
                MemorySegment pSize = arena.allocate(ValueLayout.JAVA_LONG);
                MethodHandle getUint64 = linker.downcallHandle(vtableFunc(curType, 8),
                        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
                hr = (int) getUint64.invokeExact(curType, guidFrameSize, pSize);
                long frameSize = pSize.get(ValueLayout.JAVA_LONG, 0);
                int srcW = (int) (frameSize >>> 32);
                int srcH = (int) (frameSize & 0xFFFFFFFFL);
                if (srcW <= 0 || srcH <= 0) return;

                // Đọc stride (MF_MT_DEFAULT_STRIDE)
                MemorySegment guidStride = arena.allocate(16);
                putGuid(guidStride, 0x644b4e48, (short) 0x1e02, (short) 0x4516,
                        (byte) 0xb0, (byte) 0xeb, (byte) 0xc0, (byte) 0x1c, (byte) 0xa9, (byte) 0xd4, (byte) 0x9a, (byte) 0xc6);
                MemorySegment pStride = arena.allocate(ValueLayout.JAVA_INT);
                MethodHandle getUint32 = linker.downcallHandle(vtableFunc(curType, 7),
                        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
                int hrStride = (int) getUint32.invokeExact(curType, guidStride, pStride);
                int stride = (hrStride == 0 && pStride.get(ValueLayout.JAVA_INT, 0) > 0) ? pStride.get(ValueLayout.JAVA_INT, 0) : srcW;

                // Đọc tốc độ khung hình (MF_MT_FRAME_RATE)
                MemorySegment guidFps = arena.allocate(16);
                putGuid(guidFps, 0xc459a2e8, (short) 0x3d2c, (short) 0x4e44,
                        (byte) 0xb1, (byte) 0x32, (byte) 0xfe, (byte) 0xe5, (byte) 0x15, (byte) 0x6c, (byte) 0x7b, (byte) 0xb0);
                hr = (int) getUint64.invokeExact(curType, guidFps, pSize);
                long fpsPack = pSize.get(ValueLayout.JAVA_LONG, 0);
                double fps = (hr == 0 && (fpsPack & 0xFFFFFFFFL) > 0)
                        ? (double) (fpsPack >>> 32) / (fpsPack & 0xFFFFFFFFL)
                        : 24.0;
                if (fps < 5.0 || fps > 120.0) fps = 24.0;
                long frameIntervalNanos = Math.round(1_000_000_000.0 / fps);

                // Bảng nội suy Bilinear 8-bit fixed-point (0..256) cho Luma (Y) và Chroma (UV)
                int[] lumaX0 = new int[targetW];
                int[] lumaX1 = new int[targetW];
                int[] lumaWx = new int[targetW];
                int[] chromaX0 = new int[targetW];
                int[] chromaX1 = new int[targetW];
                int[] chromaWx = new int[targetW];
                int uvPairsW = (srcW >> 1) - 1;

                for (int x = 0; x < targetW; x++) {
                    double fx = ((x + 0.5) * srcW / (double) targetW) - 0.5;
                    if (fx < 0) fx = 0;
                    int ix0 = (int) fx;
                    int ix1 = Math.min(ix0 + 1, srcW - 1);
                    lumaX0[x] = ix0;
                    lumaX1[x] = ix1;
                    lumaWx[x] = (int) Math.round((fx - ix0) * 256.0);

                    double cfx = ((x + 0.5) * (srcW >> 1) / (double) targetW) - 0.5;
                    if (cfx < 0) cfx = 0;
                    int cx0 = Math.min((int) cfx, uvPairsW);
                    int cx1 = Math.min(cx0 + 1, uvPairsW);
                    chromaX0[x] = cx0 << 1;
                    chromaX1[x] = cx1 << 1;
                    chromaWx[x] = (int) Math.round((cfx - (int) cfx) * 256.0);
                }

                int[] lumaY0 = new int[targetH];
                int[] lumaY1 = new int[targetH];
                int[] lumaWy = new int[targetH];
                int[] chromaY0 = new int[targetH];
                int[] chromaY1 = new int[targetH];
                int[] chromaWy = new int[targetH];
                int uvRowsH = (srcH >> 1) - 1;

                for (int y = 0; y < targetH; y++) {
                    double fy = ((y + 0.5) * srcH / (double) targetH) - 0.5;
                    if (fy < 0) fy = 0;
                    int iy0 = (int) fy;
                    int iy1 = Math.min(iy0 + 1, srcH - 1);
                    lumaY0[y] = iy0 * stride;
                    lumaY1[y] = iy1 * stride;
                    lumaWy[y] = (int) Math.round((fy - iy0) * 256.0);

                    double cfy = ((y + 0.5) * (srcH >> 1) / (double) targetH) - 0.5;
                    if (cfy < 0) cfy = 0;
                    int cy0 = Math.min((int) cfy, uvRowsH);
                    int cy1 = Math.min(cy0 + 1, uvRowsH);
                    chromaY0[y] = (srcH + cy0) * stride;
                    chromaY1[y] = (srcH + cy1) * stride;
                    chromaWy[y] = (int) Math.round((cfy - (int) cfy) * 256.0);
                }

                int[] lumaPlane = new int[targetW * targetH];
                int[] uPlane = new int[targetW * targetH];
                int[] vPlane = new int[targetW * targetH];
                byte[] rawBytes = new byte[stride * srcH * 3 / 2];

                MemorySegment pStreamIdx = arena.allocate(ValueLayout.JAVA_INT);
                MemorySegment pFlags = arena.allocate(ValueLayout.JAVA_INT);
                MemorySegment pTimestamp = arena.allocate(ValueLayout.JAVA_LONG);
                MemorySegment ppSample = arena.allocate(ValueLayout.ADDRESS);
                MemorySegment ppBuffer = arena.allocate(ValueLayout.ADDRESS);
                MemorySegment ppbData = arena.allocate(ValueLayout.ADDRESS);
                MemorySegment pcbMax = arena.allocate(ValueLayout.JAVA_INT);
                MemorySegment pcbCur = arena.allocate(ValueLayout.JAVA_INT);
                MemorySegment guidNull = arena.allocate(16);
                MemorySegment propVarZero = arena.allocate(24);
                propVarZero.set(ValueLayout.JAVA_SHORT_UNALIGNED, 0, (short) 20); // VT_I8
                propVarZero.set(ValueLayout.JAVA_LONG_UNALIGNED, 8, 0L);

                MethodHandle readSample = linker.downcallHandle(vtableFunc(reader, 9),
                        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT,
                                ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
                MethodHandle setCurrentPos = linker.downcallHandle(vtableFunc(reader, 8),
                        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));

                BufferedImage currentWrite = backBuffer;
                BufferedImage currentRead = frontBuffer;
                long nextFrameTime = System.nanoTime();

                while (!Thread.currentThread().isInterrupted()) {
                    if (firstFrameDecoded[0] && shouldDecode != null && !shouldDecode.getAsBoolean()) {
                        Thread.sleep(80);
                        nextFrameTime = System.nanoTime();
                        continue;
                    }

                    hr = (int) readSample.invokeExact(reader, FIRST_VIDEO_STREAM, 0, pStreamIdx, pFlags, pTimestamp, ppSample);
                    if (hr < 0) break;

                    int flags = pFlags.get(ValueLayout.JAVA_INT, 0);
                    if ((flags & MF_SOURCE_READERF_ERROR) != 0) {
                        break;
                    }
                    if ((flags & MF_SOURCE_READERF_ENDOFSTREAM) != 0) {
                        hr = (int) setCurrentPos.invokeExact(reader, guidNull, propVarZero);
                        nextFrameTime = System.nanoTime();
                        continue;
                    }

                    MemorySegment sample = ppSample.get(ValueLayout.ADDRESS, 0);
                    if (sample.equals(MemorySegment.NULL)) continue;

                    MethodHandle convertToContig = linker.downcallHandle(vtableFunc(sample, 41),
                            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
                    MethodHandle releaseSample = linker.downcallHandle(vtableFunc(sample, 2),
                            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));

                    hr = (int) convertToContig.invokeExact(sample, ppBuffer);
                    if (hr == 0) {
                        MemorySegment buffer = ppBuffer.get(ValueLayout.ADDRESS, 0);
                        if (!buffer.equals(MemorySegment.NULL)) {
                            MethodHandle lock = linker.downcallHandle(vtableFunc(buffer, 3),
                                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
                            MethodHandle unlock = linker.downcallHandle(vtableFunc(buffer, 4),
                                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
                            MethodHandle releaseBuf = linker.downcallHandle(vtableFunc(buffer, 2),
                                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));

                            hr = (int) lock.invokeExact(buffer, ppbData, pcbMax, pcbCur);
                            if (hr == 0) {
                                int curLen = pcbCur.get(ValueLayout.JAVA_INT, 0);
                                int minBytes = rawBytes.length;
                                if (curLen >= minBytes) {
                                    MemorySegment raw = ppbData.get(ValueLayout.ADDRESS, 0).reinterpret(minBytes);
                                    MemorySegment.copy(raw, ValueLayout.JAVA_BYTE, 0, rawBytes, 0, minBytes);

                                    int[] dstPixels = ((DataBufferInt) currentWrite.getRaster().getDataBuffer()).getData();
                                    decodeBilinearAndSharpen(
                                            rawBytes,
                                            dstPixels,
                                            lumaPlane,
                                            uPlane,
                                            vPlane,
                                            targetW,
                                            targetH,
                                            lumaX0, lumaX1, lumaWx,
                                            lumaY0, lumaY1, lumaWy,
                                            chromaX0, chromaX1, chromaWx,
                                            chromaY0, chromaY1, chromaWy
                                    );

                                    onFrameReady.accept(currentWrite);
                                    BufferedImage tmp = currentRead;
                                    currentRead = currentWrite;
                                    currentWrite = tmp;

                                    if (!firstFrameDecoded[0]) {
                                        synchronized (firstFrameLock) {
                                            firstFrameDecoded[0] = true;
                                            firstFrameLock.notifyAll();
                                        }
                                    }
                                }
                                hr = (int) unlock.invokeExact(buffer);
                            }
                            hr = (int) releaseBuf.invokeExact(buffer);
                        }
                    }
                    hr = (int) releaseSample.invokeExact(sample);

                    nextFrameTime += frameIntervalNanos;
                    long sleepNanos = nextFrameTime - System.nanoTime();
                    if (sleepNanos > 1_000_000L) {
                        Thread.sleep(sleepNanos / 1_000_000L, (int) (sleepNanos % 1_000_000L));
                    } else if (sleepNanos < -frameIntervalNanos * 3) {
                        nextFrameTime = System.nanoTime();
                    }
                }
            } catch (Throwable ignored) {
            } finally {
                synchronized (firstFrameLock) {
                    firstFrameLock.notifyAll();
                }
            }
        }, "OpeningVideoLoopThread");

        decoderThread.setDaemon(true);
        decoderThread.start();

        synchronized (firstFrameLock) {
            if (!firstFrameDecoded[0]) {
                try {
                    firstFrameLock.wait(600);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return firstFrameDecoded[0];
    }

    /**
     * Bước 1: Nội suy Bilinear cho cả kênh sáng (Y) và kênh màu (UV) từ khung 1280x720 sang 960x640 để khử hoàn toàn răng cưa.
     * Bước 2: Tăng độ sắc nét (Unsharp Mask / Sharpening) trên kênh Luma (Y) và chuyển sang RGB theo chuẩn HD BT.709.
     */
    private static void decodeBilinearAndSharpen(
            byte[] raw,
            int[] dstPixels,
            int[] lumaPlane,
            int[] uPlane,
            int[] vPlane,
            int targetW,
            int targetH,
            int[] lumaX0, int[] lumaX1, int[] lumaWx,
            int[] lumaY0, int[] lumaY1, int[] lumaWy,
            int[] chromaX0, int[] chromaX1, int[] chromaWx,
            int[] chromaY0, int[] chromaY1, int[] chromaWy) {
        int idx = 0;
        for (int y = 0; y < targetH; y++) {
            int ly0 = lumaY0[y];
            int ly1 = lumaY1[y];
            int lwy = lumaWy[y];
            int lwyInv = 256 - lwy;

            int cy0 = chromaY0[y];
            int cy1 = chromaY1[y];
            int cwy = chromaWy[y];
            int cwyInv = 256 - cwy;

            for (int x = 0; x < targetW; x++) {
                int lx0 = lumaX0[x];
                int lx1 = lumaX1[x];
                int lwx = lumaWx[x];
                int lwxInv = 256 - lwx;

                int y00 = raw[ly0 + lx0] & 0xFF;
                int y01 = raw[ly0 + lx1] & 0xFF;
                int y10 = raw[ly1 + lx0] & 0xFF;
                int y11 = raw[ly1 + lx1] & 0xFF;
                int yTop = y00 * lwxInv + y01 * lwx;
                int yBot = y10 * lwxInv + y11 * lwx;
                lumaPlane[idx] = (yTop * lwyInv + yBot * lwy + 32768) >> 16;

                int cx0 = chromaX0[x];
                int cx1 = chromaX1[x];
                int cwx = chromaWx[x];
                int cwxInv = 256 - cwx;

                int u00 = raw[cy0 + cx0] & 0xFF;
                int u01 = raw[cy0 + cx1] & 0xFF;
                int u10 = raw[cy1 + cx0] & 0xFF;
                int u11 = raw[cy1 + cx1] & 0xFF;
                int uTop = u00 * cwxInv + u01 * cwx;
                int uBot = u10 * cwxInv + u11 * cwx;
                uPlane[idx] = ((uTop * cwyInv + uBot * cwy + 32768) >> 16) - 128;

                int v00 = raw[cy0 + cx0 + 1] & 0xFF;
                int v01 = raw[cy0 + cx1 + 1] & 0xFF;
                int v10 = raw[cy1 + cx0 + 1] & 0xFF;
                int v11 = raw[cy1 + cx1 + 1] & 0xFF;
                int vTop = v00 * cwxInv + v01 * cwx;
                int vBot = v10 * cwxInv + v11 * cwx;
                vPlane[idx] = ((vTop * cwyInv + vBot * cwy + 32768) >> 16) - 128;

                idx++;
            }
        }

        // Bước 2: Làm nét cạnh (Luma Unsharp Mask) + Chuyển đổi chuẩn màu HD BT.709 sang RGB
        for (int y = 0; y < targetH; y++) {
            int rowOffset = y * targetW;
            boolean innerY = y > 0 && y < targetH - 1;
            for (int x = 0; x < targetW; x++) {
                int i = rowOffset + x;
                int yVal = lumaPlane[i];
                if (innerY && x > 0 && x < targetW - 1) {
                    int neighbors = lumaPlane[i - 1] + lumaPlane[i + 1]
                            + lumaPlane[i - targetW] + lumaPlane[i + targetW];
                    // Hệ số làm nét cạnh (Unsharp Mask): y + 0.45 * (4*y - neighbors) / 4
                    int detail = (yVal << 2) - neighbors;
                    yVal += (detail * 46) >> 8;
                }

                int c = yVal - 16;
                if (c < 0) c = 0;
                int u = uPlane[i];
                int v = vPlane[i];

                // Ma trận màu HD BT.709 chuẩn cho video 720p/1080p
                int c298 = 298 * c;
                int r = (c298 + 459 * v + 128) >> 8;
                int g = (c298 - 55 * u - 136 * v + 128) >> 8;
                int b = (c298 + 541 * u + 128) >> 8;

                if (r < 0) r = 0; else if (r > 255) r = 255;
                if (g < 0) g = 0; else if (g > 255) g = 255;
                if (b < 0) b = 0; else if (b > 255) b = 255;

                dstPixels[i] = 0xFF000000 | (r << 16) | (g << 8) | b;
            }
        }
    }

    private static void putGuid(MemorySegment seg, int d1, short d2, short d3, byte... d4) {
        seg.set(ValueLayout.JAVA_INT_UNALIGNED, 0, d1);
        seg.set(ValueLayout.JAVA_SHORT_UNALIGNED, 4, d2);
        seg.set(ValueLayout.JAVA_SHORT_UNALIGNED, 6, d3);
        for (int i = 0; i < 8; i++) seg.set(ValueLayout.JAVA_BYTE, 8 + i, d4[i]);
    }

    private static MemorySegment vtableFunc(MemorySegment comPtr, int index) {
        MemorySegment vtbl = comPtr.reinterpret(8).get(ValueLayout.ADDRESS, 0).reinterpret((index + 1) * 8L);
        return vtbl.get(ValueLayout.ADDRESS, index * 8L);
    }
}
