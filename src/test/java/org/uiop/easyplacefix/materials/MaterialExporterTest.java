package org.uiop.easyplacefix.materials;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MaterialExporterTest {

    @Test
    void sanitizesFileNames() {
        assertEquals("My_Base_ v2", MaterialExporter.sanitizeFileName("My/Base: v2"));
        assertEquals("materials", MaterialExporter.sanitizeFileName("   "));
        assertEquals("materials", MaterialExporter.sanitizeFileName(null));
        assertEquals(64, MaterialExporter.sanitizeFileName("x".repeat(200)).length());
    }

    @Test
    void resolvesFormatsByExtensionOrName() {
        assertEquals(MaterialExporter.Format.XLSX, MaterialExporter.Format.byName("xlsx"));
        assertEquals(MaterialExporter.Format.MARKDOWN, MaterialExporter.Format.byName("md"));
        assertEquals(MaterialExporter.Format.MARKDOWN, MaterialExporter.Format.byName("markdown"));
        assertEquals(MaterialExporter.Format.JSON, MaterialExporter.Format.byName("JSON"));
        assertNull(MaterialExporter.Format.byName("docx"));
    }
}
