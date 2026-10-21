import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * 离线自查工具：把 ProvinceOutlines 的经纬度轮廓与民俗数据点画成 PNG，
 * 用于在装到设备前确认「打点是否落在正确的省份内」。
 */
public class MapCheck {

    static final double LON_MIN = 73.0, LON_MAX = 135.6, LAT_MIN = 17.5, LAT_MAX = 54.0;

    // 与 ProvinceOutlines.kt 保持一致的轮廓（由脚本从 Kotlin 文件解析生成）
    static List<Object[]> provinces = new ArrayList<>();
    // 民俗数据点：名称, 经度, 纬度
    static List<Object[]> points = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        parseKotlin(args[0]);
        parsePoints(args[1]);

        int W = 1100, H = 780, PAD = 40;
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0xF7, 0xF2, 0xE8));
        g.fillRect(0, 0, W, H);

        double sx = (W - 2.0 * PAD) / (LON_MAX - LON_MIN);
        double sy = (H - 2.0 * PAD) / (LAT_MAX - LAT_MIN);

        // 省份轮廓
        for (Object[] p : provinces) {
            String name = (String) p[0];
            @SuppressWarnings("unchecked")
            List<double[]> pts = (List<double[]>) p[1];
            int[] xs = new int[pts.size()], ys = new int[pts.size()];
            for (int i = 0; i < pts.size(); i++) {
                xs[i] = (int) Math.round(PAD + (pts.get(i)[0] - LON_MIN) * sx);
                ys[i] = (int) Math.round(PAD + (LAT_MAX - pts.get(i)[1]) * sy);
            }
            g.setColor(new Color(0xE4, 0xDA, 0xC8));
            g.fillPolygon(xs, ys, pts.size());
            g.setColor(new Color(0xA7, 0x97, 0x85));
            g.setStroke(new BasicStroke(1.2f));
            g.drawPolygon(xs, ys, pts.size());

            // 省份名标在重心
            double cx = 0, cy = 0;
            for (double[] q : pts) { cx += q[0]; cy += q[1]; }
            cx /= pts.size(); cy /= pts.size();
            g.setColor(new Color(0x8A, 0x7C, 0x6B));
            g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
            g.drawString(name,
                (int) (PAD + (cx - LON_MIN) * sx) - name.length() * 5,
                (int) (PAD + (LAT_MAX - cy) * sy));
        }

        // 民俗数据点
        g.setFont(new Font("Microsoft YaHei", Font.BOLD, 11));
        for (Object[] pt : points) {
            String name = (String) pt[0];
            double lon = (Double) pt[1], lat = (Double) pt[2];
            int x = (int) Math.round(PAD + (lon - LON_MIN) * sx);
            int y = (int) Math.round(PAD + (LAT_MAX - lat) * sy);
            g.setColor(new Color(0x8F, 0x3A, 0x2E, 70));
            g.fillOval(x - 9, y - 9, 18, 18);
            g.setColor(new Color(0x8F, 0x3A, 0x2E));
            g.fillOval(x - 4, y - 4, 8, 8);
            g.setColor(new Color(0x4A, 0x2A, 0x20));
            g.drawString(name, x + 7, y - 5);
        }

        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        g.drawString("省数=" + provinces.size() + "  点数=" + points.size()
            + "  投影: x=(lon-73)/62.6  y=(54-lat)/36.5", PAD, H - 14);

        ImageIO.write(img, "png", new File(args[2]));
        System.out.println("已输出: " + args[2]);
    }

    /** 解析 ProvinceOutlines.kt 中的 "省名" to listOf( lon to lat, ... ) */
    static void parseKotlin(String path) throws Exception {
        String text = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(path)),
            java.nio.charset.StandardCharsets.UTF_8);
        java.util.regex.Matcher m = java.util.regex.Pattern
            .compile("\"([^\"]+)\"\\s*to\\s*listOf\\(([^)]*)\\)", java.util.regex.Pattern.DOTALL)
            .matcher(text);
        while (m.find()) {
            String name = m.group(1);
            List<double[]> pts = new ArrayList<>();
            java.util.regex.Matcher p = java.util.regex.Pattern
                .compile("([0-9]+\\.[0-9]+)\\s*to\\s*([0-9]+\\.[0-9]+)").matcher(m.group(2));
            while (p.find()) {
                pts.add(new double[]{Double.parseDouble(p.group(1)), Double.parseDouble(p.group(2))});
            }
            if (!pts.isEmpty()) provinces.add(new Object[]{name, pts});
        }
        System.out.println("解析到省份: " + provinces.size());
    }

    /** 解析 FolkCustomContent.kt 中的 longitude / latitude */
    static void parsePoints(String path) throws Exception {
        String text = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(path)),
            java.nio.charset.StandardCharsets.UTF_8);
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
            "title = \"([^\"]+)\"[\\s\\S]{0,600}?latitude = ([0-9.]+),[\\s\\S]{0,200}?longitude = ([0-9.]+)")
            .matcher(text);
        while (m.find()) {
            points.add(new Object[]{m.group(1),
                Double.parseDouble(m.group(3)), Double.parseDouble(m.group(2))});
        }
        System.out.println("解析到数据点: " + points.size());
    }
}
