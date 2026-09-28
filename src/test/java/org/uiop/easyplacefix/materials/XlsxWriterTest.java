package org.uiop.easyplacefix.materials;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XlsxWriterTest {

    @Test
    void columnNamesFollowSpreadsheetLetters() {
        assertEquals("A", XlsxWriter.columnName(0));
        assertEquals("Z", XlsxWriter.columnName(25));
        assertEquals("AA", XlsxWriter.columnName(26));
        assertEquals("AZ", XlsxWriter.columnName(51));
        assertEquals("ZZ", XlsxWriter.columnName(701));
        assertEquals("AAA", XlsxWriter.columnName(702));
    }

    @Test
    void escapeHandlesMarkupAndDropsControlCharacters() {
        assertEquals("a &amp; b &lt;c&gt; &quot;d&quot; &apos;e&apos;", XlsxWriter.escape("a & b <c> \"d\" 'e'"));
        assertEquals("tab\tline", XlsxWriter.escape("tab\tline\u0001"));
    }

    @Test
    void writesAWellFormedWorkbook(@TempDir Path directory) throws Exception {
        XlsxWriter workbook = new XlsxWriter();
        workbook.addSheet("Summary").title("Materials").row("Total", 1234L).row("Ratio", 0.5);
        workbook.addSheet("All/materials?").columnWidths(30, 10)
                .header("Item", "Count")
                .row("Oak Planks & <Stairs>", 64)
                .row("Кирпичи", 3);
        Path file = directory.resolve("out.xlsx");
        workbook.write(file);

        Map<String, String> entries = new HashMap<>();
        try (ZipFile zip = new ZipFile(file.toFile())) {
            for (ZipEntry entry : Collections.list(zip.entries())) {
                entries.put(entry.getName(), new String(zip.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8));
            }
        }

        for (String name : new String[]{"[Content_Types].xml", "_rels/.rels", "xl/workbook.xml",
                "xl/_rels/workbook.xml.rels", "xl/styles.xml", "xl/worksheets/sheet1.xml", "xl/worksheets/sheet2.xml"}) {
            assertNotNull(entries.get(name), name);
            DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new ByteArrayInputStream(entries.get(name).getBytes(StandardCharsets.UTF_8)));
        }

        String workbookXml = entries.get("xl/workbook.xml");
        assertTrue(workbookXml.contains("name=\"Summary\""));
        assertTrue(workbookXml.contains("name=\"All materials\""));
        assertTrue(workbookXml.contains("_xlnm._FilterDatabase"));

        String sheet2 = entries.get("xl/worksheets/sheet2.xml");
        assertTrue(sheet2.contains("Oak Planks &amp; &lt;Stairs&gt;"));
        assertTrue(sheet2.contains("Кирпичи"));
        assertTrue(sheet2.contains("<v>64</v>"));
        assertTrue(sheet2.contains("<autoFilter ref=\"A1:B3\"/>"));
        assertTrue(sheet2.contains("state=\"frozen\""));

        String sheet1 = entries.get("xl/worksheets/sheet1.xml");
        assertTrue(sheet1.contains("<v>1234</v>"));
        assertTrue(sheet1.contains("<v>0.5</v>"));
        assertFalse(sheet1.contains("autoFilter"));
    }
}
