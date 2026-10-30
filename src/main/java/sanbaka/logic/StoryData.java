package sanbaka.logic;

import java.util.ArrayList;
import java.util.List;
import sanbaka.model.Line;

/** Quản lý toàn bộ dữ liệu kịch bản và câu thoại của 5 ngày thi. */
public class StoryData {
    static void add(java.util.List<Line> lines, String who, String... texts) {
        boolean section = who.startsWith("NGÀY ") || who.startsWith("TRÊN ĐƯỜNG") || who.startsWith("HE ·") || who.startsWith("BE ·") || who.startsWith("SE ·") || who.startsWith("Một lúc sau");
        if (section) {
            lines.add(new Line("@" + who, ""));
            for (String text : texts) {
                int separator = text.indexOf(':');
                if (separator > 0) {
                    lines.add(new Line(text.substring(0, separator).trim(), text.substring(separator + 1).trim()));
                } else {
                    lines.add(new Line("Thắng", text));
                }
            }
            return;
        }
        lines.add(new Line(who, String.join(" ", texts)));
    }

    public static java.util.List<Line> build() {
        java.util.List<Line> lines = new java.util.ArrayList<>();
        add(lines, "Sanbaka (tin nhắn)",
            "Anh ơi, mai bắt đầu tuần thi rồi. Anh chuẩn bị hết chưa?"
        );
        add(lines, "Thắng (tin nhắn)",
            "Gần xong rồi em. Chỉ sợ sáng mai ngủ quên thôi :P"
        );
        add(lines, "Sanbaka (tin nhắn)",
            "Anh còn dám nói à? Lần trước em gọi mãi anh mới chịu dậy đấy!"
        );
        add(lines, "Thắng (tin nhắn)",
            "Tại cái giường quyến rũ anh quá mà…"
        );
        add(lines, "Sanbaka (tin nhắn)",
            "Thế này nhé, nếu anh hoàn thành tuần thi và đến C1 gặp em ngay sau môn cuối cùng, "
                    + "em sẽ thưởng cho anh một buổi hẹn hò. Mình đi ăn rồi đi chơi đâu đó nhé!"
        );
        add(lines, "Thắng (tin nhắn)",
            "Chốt kèo! Anh nhất định không ngủ quên, cũng không để em phải chờ đâu!"
        );
        add(lines, "Sanbaka (tin nhắn)",
            "Anh nhớ đấy nhé! Giờ ngủ sớm đi, mai em gọi anh dậy."
        );
        add(lines, "Thắng (tin nhắn)",
            "Dạ vợ :P"
        );
        add(lines, "NGÀY 1 · TẠI PHÒNG TRỌ",
            "Sanbaka (tin nhắn): Dậy đi! Hôm nay anh có lịch thi đấy, đừng ngủ quên nhé.",
            "Thắng (tin nhắn): Cho anh ngủ thêm năm phút thôi em…",
            "Sanbaka (tin nhắn): Năm phút của anh hôm nào chẳng thành nửa tiếng. Anh mà không dậy là em giận đấy!",
            "Thắng (tin nhắn): Anh dậy ngay đây! Đừng giận anh mà.",
            "Komori: Mày dậy rồi à? Tao tưởng phải chờ người yêu mày gọi đến lần thứ mười cơ.",
            "Thắng: Kệ tao. Có người yêu quan tâm thì mày ghen à?",
            "Komori: Ừ, tao ghen với cái chăn của mày đấy. Nó được ôm mày cả đêm.",
            "Chủ trọ: Hai đứa chuẩn bị đi thi thì nhanh lên. Đừng có đến muộn rồi lại đổ tại đường đông nhé!",
            "Thắng: Vâng ạ, cháu đi ngay đây!"
        );
        add(lines, "NGÀY 1 · SAU KHI THI",
            "Komori: Thế nào, còn sống không?",
            "Thắng: Còn. Nhưng mấy câu cuối suýt lấy mạng tao.",
            "Komori: Thế thì về ôn tiếp đi, đừng ngồi đây than thở nữa.",
            "Sanbaka (tin nhắn): Anh thi xong rồi à? Làm bài ổn không anh?",
            "Thắng (tin nhắn): Cũng tạm em ạ. Giờ anh chỉ muốn gặp em thôi.",
            "Sanbaka (tin nhắn): Cố thêm mấy ngày nữa nhé. Thi xong em sẽ thực hiện lời hứa hôm qua với anh!",
            "Thắng (tin nhắn): Em nhớ đấy nhé!"
        );
        add(lines, "NGÀY 2 · TẠI PHÒNG TRỌ",
            "Komori: Ê mày, nhìn đường ngoài kia kìa. Hôm nay đi kiểu gì đây?",
            "Thắng: Bơi đi cu",
            "Komori: Mày bơi xong tao ngồi trên mày nhể",
            "Thắng: Hmm hay là cúp cho khỏe",
            "Komori: ? Hôm nay thi đấy cu",
            "Thắng: Chết bà! Thế giờ bơi thật à?",
            "Chủ trọ: Dưới gầm giường có cái thuyền phao đấy. Lôi ra mà chèo!",
            "Thắng: ...Bác nói thật ạ?",
            "Chủ trọ: Cháu thử tìm xem, biết đâu có thật!",
            "Thắng: Đi thôi mày. Nhanh không làm cổ đông cho trường giờ!"
        );
        add(lines, "NGÀY 2 · SAU KHI THI",
            "Sanbaka: Anh đây rồi! Sao ướt hết thế này?",
            "Thắng: Anh không sao. Mưa lớn quá, suýt nữa anh thành cá luôn rồi.",
            "Sanbaka: Đồ ngốc! Em đã bảo anh đi cẩn thận mà. Mau lau khô người đi, em mua đồ ăn nóng cho anh rồi đây.",
            "Thắng: Em đợi anh lâu chưa? Còn phải đi mua đồ ăn nữa…",
            "Sanbaka: Không lâu đâu. Em chỉ sợ anh mải thi xong lại nhịn đói thôi.",
            "Thắng: Có em quan tâm thế này, đường ngập cũng đáng!",
            "Sanbaka: Anh cứ nói ngọt đi! Lần sau mà để mình ướt sũng thế này nữa, em sẽ giận thật đấy!",
            "Thắng: Dạ vợ :P"
        );
        add(lines, "NGÀY 3 · TẠI PHÒNG TRỌ",
            "Thắng: Sao nay rét thế nhể",
            "Komori: Mặc thêm áo vào đi mày. Hôm nay rét thế này mà ốm thì xác định nghỉ thi.",
            "Thắng: Hôm nay mày chu đáo thế?",
            "Komori: Tao chỉ sợ mày ốm rồi bắt tao chăm thôi.",
            "Thắng: Kê",
            "Sanbaka (tin nhắn): Hôm nay trời lạnh, anh đã có áo ấm chưa?",
            "Thắng (tin nhắn): Anh có rùi, thằng bạn cùng phòng mới cho anh",
            "Sanbaka (tin nhắn): Vậy tốt rồi, anh nhớ mặc áo ấm nhé, ra đường rét lắm đó",
            "Thắng (tin nhắn): Anh biết rùii"
        );
        add(lines, "NGÀY 3 · SAU KHI THI",
            "Thắng: Giải tích 2 đúng là không dễ thở chút nào.",
            "Sanbaka: Vất vả rồi. Anh ngồi xuống nghỉ đi, em có đồ uống ấm cho anh đây.",
            "Thắng: Được ngồi cạnh em thế này là anh hồi sức nhanh nhất rồi.",
            "Sanbaka: Miệng ngọt ghê. Nhưng tối nay phải ngủ sớm đấy, không được thức khuya ôn bài nữa!",
            "Thắng: Anh hứa!"
        );
        add(lines, "NGÀY 4 · TẠI PHÒNG TRỌ",
            "Thắng: Hôm nay nóng như đổ lửa. Tao chỉ muốn nằm nhà bật điều hòa thôi.",
            "Komori: Thế mày định để giảng viên phát đề qua bưu điện à cu?",
            "Thắng: Tao đang cân nhắc đề xuất phương án đó.",
            "Komori: Mơ tiếp đi, đến giờ đi thi rồi!"
        );
        add(lines, "NGÀY 4 · SAU KHI THI",
            "Thắng: Toán rời rạc mà còn khó hiểu hơn cả cuộc đời tao.",
            "Komori: Mày cứ làm như tối qua mày ôn bài chăm chỉ lắm ấy.",
            "Thắng: Tao có ôn thật!",
            "Komori: Ừ, ôn được mười phút rồi ngủ gục trên bàn.",
            "Thắng: Mày nín!",
            "Sanbaka (tin nhắn): Anh thi xong chưa? Hôm nay có khó lắm không anh?",
            "Thắng (tin nhắn): Cũng hơi khó, nhưng anh làm hết rồi. Còn 1 ngày cuối thôi!",
            "Sanbaka (tin nhắn): Anh giỏi lắm! Nhớ nghỉ ngơi một chút rồi hãy ôn tiếp nhé.",
            "Thắng (tin nhắn): Có em động viên, anh thấy nhẹ nhõm hơn nhiều."
        );
        add(lines, "NGÀY 5 · TẠI PHÒNG TRỌ",
            "Komori: Ngày cuối rồi đấy. Thi xong thì đi gặp vợ mày đi, nghe mày than nhớ vợ cả tuần tao phát ngán rồi.",
            "Thắng: Ghen tị thì tìm người yêu đi em.",
            "Sanbaka (tin nhắn): Anh ơi, hôm nay là ngày thi cuối cùng rồi! Cố lên nhé, "
                    + "thi xong mình thực hiện lời hứa từ đầu tuần thôi. Em chờ anh ở C1 đấy!",
            "Thắng (tin nhắn): Anh nhớ mà! Hôm nay nhất định anh sẽ đến đúng giờ. Không để em chờ lâu đâu!",
            "Thắng: …",
            "Mày cười cái chó gì, nhanh tao còn đi thi!"
        );
        add(lines, "TRÊN ĐƯỜNG ĐẾN TRƯỜNG · TẮC ĐƯỜNG",
            "Thắng: Ủa? Sao xe đứng im hết thế này?",
            "Mới đi được một phần ba đường thôi mà! Đừng nói là cả thành phố quyết định đi học cùng một giờ nhé?!",
            "5 phút sau, vẫn không xe nào nhúc nhích",
            "Thắng (điện thoại): Alo, mày đang ở đâu đấy?",
            "Komori (điện thoại): Ở trọ chứ ở đâu cu, nay tao ko có lịch thi",
            "Thắng (điện thoại): Tao đang tắc đường, đứng đây nãy giờ rồi. Cứ thế này thì tao đến trường bằng đường tâm linh mất!",
            "Komori (điện thoại): Thế mày muốn tao làm gì? Ra đấy cõng mày à?",
            "Thắng (điện thoại): Mày mà cõng được tao qua cả đống xe này thì tao phong mày làm ân nhân cả đời!",
            "Komori (điện thoại): Được. Đứng yên đấy, tao đến đón.",
            "Thắng (điện thoại): Hả? Mày có cách thật à?",
            "Komori (điện thoại): Tin tao đi. Hôm nay tao sẽ cho mày thấy tình bạn đích thực là thế nào!",
            "Một lúc sau, tiếng cánh quạt trực thăng vang lên từ phía trên.",
            "Thắng: …Cái méo gì thế?!",
            "Komori (loa ngoài): Ê thằng kia! Ngẩng đầu lên! Thấy chiếc trực thăng không?",
            "Thắng: THẰNG ĐIÊN! MÀY LẤY ĐÂU RA TRỰC THĂNG THẾ?!",
            "Komori: Tao bảo rồi, hôm nay tao sẽ cho mày thấy tình bạn đích thực!",
            "Thắng: MÀY BIẾT LÁI TRỰC THĂNG TỪ BAO GIỜ?!",
            "Komori: Từ lúc mày còn đang ngủ nướng ấy!",
            "Thắng: THẾ SAO MÀY KHÔNG KỂ TAO NGHE?!",
            "Komori: Mày có bao giờ dậy đúng giờ để nghe tao kể chuyện đâu!",
            "Thắng: …Được rồi, vụ này tao không cãi được.",
            "Komori: Bám chắc vào nhé! Tao đưa mày đến trường.",
            "Thắng: Tao thề sau kỳ thi tao sẽ viết hẳn một bài báo cáo về độ bất thường của mày!",
            "Komori: Viết đi, nhớ ghi tên tao vào phần cảm ơn!"
        );
        add(lines, "NGÀY 5 · SAU KHI THI",
            "Thắng: Cuối cùng cũng xong! Tao chính thức được trả lại cuộc đời rồi.",
            "Komori: Chúc mừng thằng cu, còn tao thì được trả từ hôm trước rồi. Giờ thì đi gặp vợ mày đi cu.",
            "Thắng: Ừ, tao đi đây! Nhưng mày, tao còn nhiều chuyện để hỏi đấy, chờ đi. Mày chưa xong vụ trực thăng với tao đâu.",
            "Komori: Ừ, hỏi gì cứ hỏi, giờ lượn đi em."
        );
        add(lines, "Một lúc sau...",
            "Sanbaka: Anh đến rồi! Em biết anh sẽ làm được mà!",
            "Thắng: Anh đã hứa với em thì nhất định phải đến chứ.",
            "Sanbaka: Tuần này anh vất vả rồi. Giờ mình cùng đi ăn gì nhé, em bao!",
            "Thắng: Anh chờ câu này suốt cả tuần đấy.",
            "Sanbaka: Nhưng lần sau vẫn phải chăm chỉ như thế, nghe chưa?",
            "Thắng: Được, miễn là người đi ăn cùng anh vẫn là em.",
            "Sanbaka: Đồ ngốc! Đi thôi! ><"
        );
        add(lines, "SE · SECRET ENDING",
            "Dẫn truyện: Sau khi tạm biệt Sanbaka, Thắng quay lại ghế đá gần sân trường để nghỉ ngơi. Cậu chợt nhớ đến chiếc máy chơi game cũ nhặt được trên đường mấy hôm trước.",
            "Thắng: À, đúng rồi! Mấy hôm nay bận thi quá, mình còn chưa xem kỹ cái máy chơi game nhặt được hôm trước nữa.",
            "Dẫn truyện: Thắng lấy chiếc máy ra khỏi ba lô, tò mò bật nguồn. Màn hình sáng lên, hiển thị giao diện của một trò chơi kỳ lạ.",
            "Dẫn truyện: Một tiếng tách vang lên. Ngăn nhỏ phía dưới chiếc máy bật mở, bên trong là một chiếc hộp nhung màu đỏ. Thắng ngạc nhiên cầm chiếc hộp lên rồi mở ra. Một chiếc nhẫn lấp lánh nằm ngay ngắn bên trong.",
            "Thắng: Hả?... Khoan đã, cái gì đây?!",
            "Dẫn truyện: Thắng cầm chiếc nhẫn lên, ngắm nghía một lúc. Trên màn hình máy chơi game xuất hiện thêm một dòng chữ.",
            "Màn hình máy chơi game: NHIỆM VỤ CUỐI CÙNG ĐÃ HOÀN THÀNH.",
            "Thắng: Cái trò gì kỳ quặc thế này…",
            "Dẫn truyện: Đúng lúc đó, một giọng nói quen thuộc vang lên từ phía sau.",
            "Sanbaka: Thế nào anh? Thích món quà em chuẩn bị không?",
            "Thắng: Ơ?!",
            "Dẫn truyện: Thắng giật mình quay lại. Sanbaka đang đứng sau ghế đá, hai tay giấu sau lưng, nở một nụ cười tinh nghịch.",
            "Thắng: SANBAKA?! Sao em lại ở đây?!",
            "Sanbaka: Hì hì! Bất ngờ chưa?",
            "Thắng: Khoan, đừng nói với anh là…",
            "Sanbaka: Đúng rồi đó! Chiếc máy chơi game là do em cố tình để ở chỗ anh có thể nhặt được đấy!",
            "Thắng: CÁI GÌ?!",
            "Sanbaka: Em đã chuẩn bị trò chơi này từ trước rồi. Em muốn xem anh có đủ kiên nhẫn để hoàn thành tất cả thử thách không thôi!",
            "Thắng: Thế mấy màn khó kinh khủng kia cũng là do em làm à?!",
            "Sanbaka: Bí mật! Nhưng anh vẫn thắng được đấy thôi.",
            "Thắng: Trời ơi… Anh đã tưởng mình nhặt được đồ của ai đánh rơi cơ đấy!",
            "Sanbaka: Vậy anh có thích không?",
            "Dẫn truyện: Thắng nhìn chiếc nhẫn trong tay, rồi nhìn Sanbaka. Cậu dần hiểu ra ý nghĩa của món quà này. Sanbaka không nói thêm gì, chỉ dịu dàng nhìn cậu, chờ đợi phản ứng.",
            "Thắng: Sanbaka này…",
            "Sanbaka: Dạ?",
            "Dẫn truyện: Thắng từ từ đứng dậy, cẩn thận cất chiếc máy chơi game sang một bên. Cậu nắm lấy chiếc hộp nhẫn, hít một hơi thật sâu rồi bước đến trước mặt Sanbaka.",
            "Sanbaka: Ơ? Anh định làm gì thế?",
            "Dẫn truyện: Thắng bất ngờ quỳ một gối xuống trước mặt cô, mở chiếc hộp nhẫn ra.",
            "Sanbaka: …!",
            "Thắng: Sanbaka, anh biết anh không phải một người hoàn hảo. Anh hay ngủ nướng, đôi khi hậu đậu, lại còn thường xuyên khiến em phải lo lắng.",
            "Thắng: Nhưng từ khi có em, anh luôn có thêm động lực để cố gắng. Những ngày thi cử mệt mỏi cũng trở nên dễ chịu hơn chỉ vì một tin nhắn của em.",
            "Thắng: Và dù có chuyện gì xảy ra, người đầu tiên anh muốn chia sẻ niềm vui hay nỗi buồn vẫn luôn là em.",
            "Dẫn truyện: Thắng ngẩng đầu nhìn Sanbaka, ánh mắt chân thành.",
            "Thắng: Anh không biết tương lai sẽ có những khó khăn gì đang chờ chúng ta. Nhưng anh biết anh muốn cùng em đối mặt với tất cả những điều đó.",
            "Thắng: Vì vậy…",
            "Dẫn truyện: Thắng đưa chiếc nhẫn về phía Sanbaka.",
            "Thắng: Sanbaka, em có đồng ý lấy anh không? Anh muốn được ở bên em, chăm sóc em và cùng em đi hết quãng đời còn lại.",
            "Dẫn truyện: Sanbaka đứng sững trong giây lát. Đôi mắt cô mở to vì bất ngờ, rồi nhanh chóng ánh lên niềm hạnh phúc. Cô chẳng hề do dự, lập tức đưa tay ra trước mặt Thắng.",
            "Sanbaka: Em đồng ý!",
            "Thắng: …Hả?",
            "Sanbaka: Em nói là em đồng ý! Anh còn định để em chờ đến bao giờ nữa hả?!",
            "Thắng: Ơ?! Anh còn chưa nói hết mà!",
            "Sanbaka: Không cần đâu! Em biết câu trả lời của mình từ lâu rồi!",
            "Sanbaka: Em đã chờ ngày này lâu lắm rồi đấy, đồ ngốc!",
            "Dẫn truyện: Thắng run run lấy chiếc nhẫn ra khỏi hộp rồi nhẹ nhàng đeo vào ngón áp út của Sanbaka. Cô ngắm chiếc nhẫn, sau đó chìa tay ra trước mặt cậu với vẻ đắc ý.",
            "Sanbaka: Đẹp không anh?",
            "Thắng: Đẹp lắm. Nhưng mà…",
            "Sanbaka: Nhưng mà sao?",
            "Thắng: Anh có cảm giác mình vừa bị em lừa vào một nhiệm vụ bí mật mà phần thưởng cuối cùng là kết hôn ấy!",
            "Sanbaka: Hì hì! Thế anh có muốn từ chối phần thưởng không?",
            "Thắng: Không đời nào!",
            "Dẫn truyện: Thắng đứng dậy, ôm Sanbaka vào lòng. Cô cũng vòng tay ôm chặt lấy cậu, vui vẻ tựa đầu vào ngực anh.",
            "Sanbaka: Em yêu anh, Thắng.",
            "Thắng: Anh cũng yêu em, Sanbaka.",
            "Dẫn truyện: Chuyển cảnh: Lễ đường.",
            "Dẫn truyện: Lễ đường được trang trí bằng hoa trắng và ánh nến. Thắng trong bộ vest lịch lãm đứng chờ ở cuối lối đi. Khi tiếng nhạc vang lên, Sanbaka xuất hiện trong chiếc váy cưới trắng tinh khôi, mỉm cười bước về phía cậu.",
            "Thắng: Sanbaka…",
            "Sanbaka: Em đến rồi đây!",
            "Thắng: Hôm nay em đẹp quá.",
            "Sanbaka: Anh cũng đẹp trai lắm, chú rể à!",
            "Dẫn truyện: Hai người nắm tay nhau trước sự chứng kiến của gia đình và bạn bè.",
            "Người chủ trì: Thắng, con có đồng ý nhận Sanbaka làm vợ, yêu thương và đồng hành cùng cô ấy trong suốt cuộc đời mình không?",
            "Thắng: Con đồng ý!",
            "Người chủ trì: Sanbaka, con có đồng ý nhận Thắng làm chồng, yêu thương và đồng hành cùng anh ấy trong suốt cuộc đời mình không?",
            "Sanbaka: Con đồng ý!",
            "Người chủ trì: Vậy xin chúc mừng hai con. Từ hôm nay, hai con chính thức trở thành vợ chồng.",
            "Dẫn truyện: Tiếng vỗ tay vang lên khắp lễ đường. Thắng nắm tay Sanbaka, nhìn cô bằng ánh mắt dịu dàng.",
            "Thắng: Cảm ơn em vì đã xuất hiện trong cuộc đời anh.",
            "Sanbaka: Em mới là người phải cảm ơn anh. Cảm ơn anh vì đã vượt qua tất cả thử thách và tìm đến em.",
            "Thắng: Vậy từ giờ, anh sẽ tiếp tục hoàn thành mọi nhiệm vụ mà em giao nhé?",
            "Sanbaka: Tất nhiên rồi! Nhưng nhiệm vụ lần này kéo dài cả đời đấy!",
            "Thắng: Anh chấp nhận!"
        );
        return lines;
    }
}

