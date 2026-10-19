package sanbaka;

import java.util.ArrayList;
import java.util.List;

class StoryData {
    static void add(java.util.List<Line> lines, String who, String... texts) {
        boolean section = who.startsWith("NGÀY ") || who.startsWith("TRÊN ĐƯỜNG") || who.startsWith("HE ·") || who.startsWith("BE ·") || who.startsWith("Một lúc sau");
        if (section) {
            lines.add(new Line("@" + who, ""));
            for (String text : texts) {
                int separator = text.indexOf(':');
                if (separator > 0) {
                    lines.add(new Line( text.substring(0, separator).trim(), text.substring(separator + 1).trim()));
                }
                else {
                    lines.add(new Line("Thắng", text));
                }
            }
            return;
        }
        lines.add(new Line(who, String.join(" ", texts)));
    }

    static java.util.List<Line> build() {
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
            "Nam: Mày dậy rồi à? Tao tưởng phải chờ người yêu mày gọi đến lần thứ mười cơ.",
            "Thắng: Kệ tao. Có người yêu quan tâm thì mày ghen à?",
            "Nam: Ừ, tao ghen với cái chăn của mày đấy. Nó được ôm mày cả đêm.",
            "Chủ trọ: Hai đứa chuẩn bị đi thi thì nhanh lên. Đừng có đến muộn rồi lại đổ tại đường đông nhé!",
            "Thắng: Vâng ạ, cháu đi ngay đây!"
        );
        add(lines, "NGÀY 1 · SAU KHI THI",
            "Nam: Thế nào, còn sống không?",
            "Thắng: Còn. Nhưng mấy câu cuối suýt lấy mạng tao.",
            "Nam: Thế thì về ôn tiếp đi, đừng ngồi đây than thở nữa.",
            "Sanbaka (tin nhắn): Anh thi xong rồi à? Làm bài ổn không anh?",
            "Thắng (tin nhắn): Cũng tạm em ạ. Giờ anh chỉ muốn gặp em thôi.",
            "Sanbaka (tin nhắn): Cố thêm mấy ngày nữa nhé. Thi xong em sẽ thực hiện lời hứa hôm qua với anh!",
            "Thắng (tin nhắn): Em nhớ đấy nhé!"
        );
        add(lines, "NGÀY 2 · TẠI PHÒNG TRỌ",
            "Nam: Ê mày, nhìn đường ngoài kia kìa. Hôm nay đi kiểu gì đây?",
            "Thắng: Bơi đi cu",
            "Nam: Mày bơi xong tao ngồi trên mày nhể",
            "Thắng: Hmm hay là cúp cho khỏe",
            "Nam: ? Hôm nay thi đấy cu",
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
            "Nam: Mặc thêm áo vào đi mày. Hôm nay rét thế này mà ốm thì xác định nghỉ thi.",
            "Thắng: Hôm nay mày chu đáo thế?",
            "Nam: Tao chỉ sợ mày ốm rồi bắt tao chăm thôi.",
            "Thắng: Kê"
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
            "Nam: Thế mày định để giảng viên phát đề qua bưu điện à cu?",
            "Thắng: Tao đang cân nhắc đề xuất phương án đó.",
            "Nam: Mơ tiếp đi, đến giờ đi thi rồi!"
        );
        add(lines, "NGÀY 4 · SAU KHI THI",
            "Thắng: Toán rời rạc mà còn khó hiểu hơn cả cuộc đời tao.",
            "Nam: Mày cứ làm như tối qua mày ôn bài chăm chỉ lắm ấy.",
            "Thắng: Tao có ôn thật!",
            "Nam: Ừ, ôn được mười phút rồi ngủ gục trên bàn.",
            "Thắng: Mày nín!",
            "Sanbaka (tin nhắn): Anh thi xong chưa? Hôm nay có khó lắm không anh?",
            "Thắng (tin nhắn): Cũng hơi khó, nhưng anh làm hết rồi. Còn 1 ngày cuối thôi!",
            "Sanbaka (tin nhắn): Anh giỏi lắm! Nhớ nghỉ ngơi một chút rồi hãy ôn tiếp nhé.",
            "Thắng (tin nhắn): Có em động viên, anh thấy nhẹ nhõm hơn nhiều."
        );
        add(lines, "NGÀY 5 · TẠI PHÒNG TRỌ",
            "Nam: Ngày cuối rồi đấy. Thi xong thì đi gặp vợ mày đi, nghe mày than nhớ vợ cả tuần tao phát ngán rồi.",
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
            "Nam (điện thoại): Ở trọ chứ ở đâu cu, nay tao ko có lịch thi",
            "Thắng (điện thoại): Tao đang tắc đường, đứng đây nãy giờ rồi. Cứ thế này thì tao đến trường bằng đường tâm linh mất!",
            "Nam (điện thoại): Thế mày muốn tao làm gì? Ra đấy cõng mày à?",
            "Thắng (điện thoại): Mày mà cõng được tao qua cả đống xe này thì tao phong mày làm ân nhân cả đời!",
            "Nam (điện thoại): Được. Đứng yên đấy, tao đến đón.",
            "Thắng (điện thoại): Hả? Mày có cách thật à?",
            "Nam (điện thoại): Tin tao đi. Hôm nay tao sẽ cho mày thấy tình bạn đích thực là thế nào!",
            "Một lúc sau, tiếng cánh quạt trực thăng vang lên từ phía trên.",
            "Thắng: …Cái méo gì thế?!",
            "Nam (loa ngoài): Ê thằng kia! Ngẩng đầu lên! Thấy chiếc trực thăng không?",
            "Thắng: THẰNG ĐIÊN! MÀY LẤY ĐÂU RA TRỰC THĂNG THẾ?!",
            "Nam: Tao bảo rồi, hôm nay tao sẽ cho mày thấy tình bạn đích thực!",
            "Thắng: MÀY BIẾT LÁI TRỰC THĂNG TỪ BAO GIỜ?!",
            "Nam: Từ lúc mày còn đang ngủ nướng ấy!",
            "Thắng: THẾ SAO MÀY KHÔNG KỂ TAO NGHE?!",
            "Nam: Mày có bao giờ dậy đúng giờ để nghe tao kể chuyện đâu!",
            "Thắng: …Được rồi, vụ này tao không cãi được.",
            "Nam: Bám chắc vào nhé! Tao đưa mày đến trường.",
            "Thắng: Tao thề sau kỳ thi tao sẽ viết hẳn một bài báo cáo về độ bất thường của mày!",
            "Nam: Viết đi, nhớ ghi tên tao vào phần cảm ơn!"
        );
        add(lines, "NGÀY 5 · SAU KHI THI",
            "Thắng: Cuối cùng cũng xong! Tao chính thức được trả lại cuộc đời rồi.",
            "Nam: Chúc mừng thằng cu, còn tao thì được trả từ hôm trước rồi. Giờ thì đi gặp vợ mày đi cu.",
            "Thắng: Ừ, tao đi đây! Nhưng mày, tao còn nhiều chuyện để hỏi đấy, chờ đi. Mày chưa xong vụ trực thăng với tao đâu.",
            "Nam: Ừ, hỏi gì cứ hỏi, giờ lượn đi em."
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
            return lines;
    }
}
