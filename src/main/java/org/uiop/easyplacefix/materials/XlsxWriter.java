package org.uiop.easyplacefix.materials;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class XlsxWriter {
    private static final int STYLE_DEFAULT = 0;
    private static final int STYLE_HEADER = 1;
    private static final int STYLE_INTEGER = 2;
    private static final int STYLE_DECIMAL = 3;
    private static final int STYLE_TITLE = 4;

    private final List<Sheet> sheets = new ArrayList<>();

    public Sheet addSheet(String name) {
        Sheet sheet = new Sheet(sanitizeSheetName(name));
        this.sheets.add(sheet);
        return sheet;
    }

    public void write(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        try (OutputStream out = Files.newOutputStream(path);
             ZipOutputStream zip = new ZipOutputStream(out, StandardCharsets.UTF_8)) {
            entry(zip, "[Content_Types].xml", contentTypes());
            entry(zip, "_rels/.rels", rootRels());
            entry(zip, "xl/workbook.xml", workbook());
            entry(zip, "xl/_rels/workbook.xml.rels", workbookRels());
            entry(zip, "xl/styles.xml", styles());
            for (int i = 0; i < this.sheets.size(); i++) {
                entry(zip, "xl/worksheets/sheet" + (i + 1) + ".xml", this.sheets.get(i).toXml());
            }
        }
    }

    private static void entry(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private String contentTypes() {
        StringBuilder xml = new StringBuilder(xmlHeader())
                .append("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">")
                .append("<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>")
                .append("<Default Extension=\"xml\" ContentType=\"application/xml\"/>")
                .append("<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>")
                .append("<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>");
        for (int i = 0; i < this.sheets.size(); i++) {
            xml.append("<Override PartName=\"/xl/worksheets/sheet").append(i + 1)
                    .append(".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>");
        }
        return xml.append("</Types>").toString();
    }

    private static String rootRels() {
        return xmlHeader()
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                + "</Relationships>";
    }

    private String workbook() {
        StringBuilder xml = new StringBuilder(xmlHeader())
                .append("<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" ")
                .append("xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><sheets>");
        for (int i = 0; i < this.sheets.size(); i++) {
            xml.append("<sheet name=\"").append(escape(this.sheets.get(i).name)).append("\" sheetId=\"")
                    .append(i + 1).append("\" r:id=\"rId").append(i + 1).append("\"/>");
        }
        xml.append("</sheets><definedNames>");
        for (int i = 0; i < this.sheets.size(); i++) {
            Sheet sheet = this.sheets.get(i);
            if (sheet.autoFilterRange() != null) {
                xml.append("<definedName name=\"_xlnm._FilterDatabase\" localSheetId=\"").append(i)
                        .append("\" hidden=\"1\">'").append(escape(sheet.name.replace("'", "''"))).append("'!")
                        .append(absoluteRange(sheet.autoFilterRange())).append("</definedName>");
            }
        }
        return xml.append("</definedNames></workbook>").toString();
    }

    private String workbookRels() {
        StringBuilder xml = new StringBuilder(xmlHeader())
                .append("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">");
        for (int i = 0; i < this.sheets.size(); i++) {
            xml.append("<Relationship Id=\"rId").append(i + 1)
                    .append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet")
                    .append(i + 1).append(".xml\"/>");
        }
        xml.append("<Relationship Id=\"rId").append(this.sheets.size() + 1)
                .append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>");
        return xml.append("</Relationships>").toString();
    }

    private static String styles() {
        return xmlHeader()
                + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<numFmts count=\"1\"><numFmt numFmtId=\"164\" formatCode=\"0.00\"/></numFmts>"
                + "<fonts count=\"3\">"
                + "<font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
                + "<font><b/><sz val=\"11\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/></font>"
                + "<font><b/><sz val=\"14\"/><name val=\"Calibri\"/></font>"
                + "</fonts>"
                + "<fills count=\"3\">"
                + "<fill><patternFill patternType=\"none\"/></fill>"
                + "<fill><patternFill patternType=\"gray125\"/></fill>"
                + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF2F5597\"/><bgColor indexed=\"64\"/></patternFill></fill>"
                + "</fills>"
                + "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>"
                + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                + "<cellXfs count=\"5\">"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
                + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\"/>"
                + "<xf numFmtId=\"3\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\"/>"
                + "<xf numFmtId=\"164\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\"/>"
                + "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/>"
                + "</cellXfs>"
                + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
                + "</styleSheet>";
    }

    private static String xmlHeader() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
    }

    static String escape(String value) {
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                case '\'' -> out.append("&apos;");
                default -> {
                    if (c == '\t' || c == '\n' || c == '\r' || c >= 0x20) {
                        out.append(c);
                    }
                }
            }
        }
        return out.toString();
    }

    private static String sanitizeSheetName(String name) {
        String cleaned = name.replaceAll("[\\\\/?*\\[\\]:]", " ").trim();
        if (cleaned.isEmpty()) {
            cleaned = "Sheet";
        }
        return cleaned.length() > 31 ? cleaned.substring(0, 31) : cleaned;
    }

    static String columnName(int index) {
        StringBuilder name = new StringBuilder();
        int value = index + 1;
        while (value > 0) {
            int remainder = (value - 1) % 26;
            name.insert(0, (char) ('A' + remainder));
            value = (value - 1) / 26;
        }
        return name.toString();
    }

    private static String absoluteRange(String range) {
        String[] parts = range.split(":");
        return absoluteCell(parts[0]) + ":" + absoluteCell(parts[1]);
    }

    private static String absoluteCell(String cell) {
        int split = 0;
        while (split < cell.length() && Character.isLetter(cell.charAt(split))) {
            split++;
        }
        return "$" + cell.substring(0, split) + "$" + cell.substring(split);
    }

    public static final class Sheet {
        private final String name;
        private final List<List<Cell>> rows = new ArrayList<>();
        private final List<Double> columnWidths = new ArrayList<>();
        private int headerRow = -1;
        private int columnCount;

        private Sheet(String name) {
            this.name = name;
        }

        public Sheet columnWidths(double... widths) {
            this.columnWidths.clear();
            for (double width : widths) {
                this.columnWidths.add(width);
            }
            return this;
        }

        public Sheet title(String text) {
            List<Cell> row = new ArrayList<>();
            row.add(new Cell(text, null, STYLE_TITLE));
            this.rows.add(row);
            return this;
        }

        public Sheet blank() {
            this.rows.add(new ArrayList<>());
            return this;
        }

        public Sheet header(String... names) {
            List<Cell> row = new ArrayList<>();
            for (String headerName : names) {
                row.add(new Cell(headerName, null, STYLE_HEADER));
            }
            this.headerRow = this.rows.size();
            this.columnCount = Math.max(this.columnCount, names.length);
            this.rows.add(row);
            return this;
        }

        public Sheet row(Object... values) {
            List<Cell> row = new ArrayList<>();
            for (Object value : values) {
                if (value instanceof Double || value instanceof Float) {
                    row.add(new Cell(null, ((Number) value).doubleValue(), STYLE_DECIMAL));
                } else if (value instanceof Number number) {
                    row.add(new Cell(null, number.doubleValue(), STYLE_INTEGER));
                } else {
                    row.add(new Cell(value == null ? "" : value.toString(), null, STYLE_DEFAULT));
                }
            }
            this.columnCount = Math.max(this.columnCount, values.length);
            this.rows.add(row);
            return this;
        }

        private String autoFilterRange() {
            if (this.headerRow < 0 || this.columnCount == 0) {
                return null;
            }
            int lastRow = Math.max(this.rows.size(), this.headerRow + 1);
            return columnName(0) + (this.headerRow + 1) + ":" + columnName(this.columnCount - 1) + lastRow;
        }

        private String toXml() {
            StringBuilder xml = new StringBuilder(xmlHeader())
                    .append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
            if (this.headerRow >= 0) {
                int topRow = this.headerRow + 2;
                xml.append("<sheetViews><sheetView workbookViewId=\"0\"><pane ySplit=\"").append(this.headerRow + 1)
                        .append("\" topLeftCell=\"A").append(topRow)
                        .append("\" activePane=\"bottomLeft\" state=\"frozen\"/></sheetView></sheetViews>");
            }
            if (!this.columnWidths.isEmpty()) {
                xml.append("<cols>");
                for (int i = 0; i < this.columnWidths.size(); i++) {
                    xml.append("<col min=\"").append(i + 1).append("\" max=\"").append(i + 1)
                            .append("\" width=\"").append(this.columnWidths.get(i)).append("\" customWidth=\"1\"/>");
                }
                xml.append("</cols>");
            }
            xml.append("<sheetData>");
            for (int r = 0; r < this.rows.size(); r++) {
                List<Cell> row = this.rows.get(r);
                xml.append("<row r=\"").append(r + 1).append("\">");
                for (int c = 0; c < row.size(); c++) {
                    Cell cell = row.get(c);
                    String ref = columnName(c) + (r + 1);
                    if (cell.number != null) {
                        xml.append("<c r=\"").append(ref).append("\" s=\"").append(cell.style).append("\"><v>")
                                .append(formatNumber(cell.number)).append("</v></c>");
                    } else {
                        xml.append("<c r=\"").append(ref).append("\" s=\"").append(cell.style)
                                .append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                                .append(escape(cell.text)).append("</t></is></c>");
                    }
                }
                xml.append("</row>");
            }
            xml.append("</sheetData>");
            String filter = autoFilterRange();
            if (filter != null) {
                xml.append("<autoFilter ref=\"").append(filter).append("\"/>");
            }
            return xml.append("</worksheet>").toString();
        }

        private static String formatNumber(double value) {
            if (value == Math.rint(value) && Math.abs(value) < 1.0E15) {
                return Long.toString((long) value);
            }
            return Double.toString(value);
        }
    }

    private record Cell(String text, Double number, int style) {
    }
}
