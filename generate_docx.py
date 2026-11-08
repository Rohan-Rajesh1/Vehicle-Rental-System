#!/usr/bin/env python3
import os
import sys
import zipfile
import re
from xml.sax.saxutils import escape

def make_run(text, bold=False, italic=False, color="1E293B", size=22, font="Arial"):
    escaped = escape(text)
    rpr = ['<w:rPr>']
    rpr.append(f'<w:rFonts w:ascii="{font}" w:hAnsi="{font}"/>')
    if bold:
        rpr.append('<w:b/>')
    if italic:
        rpr.append('<w:i/>')
    if color:
        rpr.append(f'<w:color w:val="{color}"/>')
    if size:
        rpr.append(f'<w:sz w:val="{size}"/>')
    rpr.append('</w:rPr>')
    return f'<w:r>{"".join(rpr)}<w:t xml:space="preserve">{escaped}</w:t></w:r>'

def make_para(runs_xml, align="left", space_before=100, space_after=100, style=None):
    ppr = ['<w:pPr>']
    if style:
        ppr.append(f'<w:pStyle w:val="{style}"/>')
    if align != "left":
        ppr.append(f'<w:jc w:val="{align}"/>')
    ppr.append(f'<w:spacing w:before="{space_before}" w:after="{space_after}" w:line="276" w:lineRule="auto"/>')
    ppr.append('</w:pPr>')
    return f'<w:p>{"".join(ppr)}{runs_xml}</w:p>'

def make_page_break():
    return '<w:p><w:r><w:br w:type="page"/></w:r></w:p>'

def make_heading(text, level=1):
    sizes = {1: 34, 2: 28, 3: 24, 4: 22}
    colors = {1: "0F172A", 2: "1E3A8A", 3: "2563EB", 4: "334155"}
    befores = {1: 360, 2: 260, 3: 200, 4: 160}
    afters = {1: 140, 2: 120, 3: 100, 4: 80}
    
    run = make_run(text, bold=True, color=colors.get(level, "0F172A"), size=sizes.get(level, 24), font="Arial")
    return make_para(run, align="left", space_before=befores.get(level, 200), space_after=afters.get(level, 100))

def make_code_block(code_lines, language="java"):
    paras = []
    # Frame container
    for line in code_lines:
        run = make_run(line, color="0F172A", size=18, font="Consolas")
        ppr = (
            '<w:pPr>'
            '<w:shd w:val="clear" w:color="auto" w:fill="F1F5F9"/>'
            '<w:spacing w:before="20" w:after="20" w:line="240" w:lineRule="auto"/>'
            '<w:pBdr>'
            '<w:left w:val="single" w:sz="18" w:space="15" w:color="3B82F6"/>'
            '</w:pBdr>'
            '</w:pPr>'
        )
        paras.append(f'<w:p>{ppr}{run}</w:p>')
    return "".join(paras)

def make_table(rows, is_header=True):
    tbl = ['<w:tbl>']
    tbl.append(
        '<w:tblPr>'
        '<w:tblW w:w="9360" w:type="dxa"/>'
        '<w:tblBorders>'
        '<w:top w:val="single" w:sz="4" w:space="0" w:color="CBD5E1"/>'
        '<w:left w:val="single" w:sz="4" w:space="0" w:color="CBD5E1"/>'
        '<w:bottom w:val="single" w:sz="4" w:space="0" w:color="CBD5E1"/>'
        '<w:right w:val="single" w:sz="4" w:space="0" w:color="CBD5E1"/>'
        '<w:insideH w:val="single" w:sz="4" w:space="0" w:color="E2E8F0"/>'
        '<w:insideV w:val="single" w:sz="4" w:space="0" w:color="E2E8F0"/>'
        '</w:tblBorders>'
        '</w:tblPr>'
    )
    for r_idx, row in enumerate(rows):
        tbl.append('<w:tr>')
        is_h = (r_idx == 0 and is_header)
        for cell in row:
            bg_fill = "1E293B" if is_h else ("F8FAFC" if r_idx % 2 == 1 else "FFFFFF")
            text_color = "FFFFFF" if is_h else "1E293B"
            tbl.append('<w:tc>')
            tbl.append(
                f'<w:tcPr>'
                f'<w:shd w:val="clear" w:color="auto" w:fill="{bg_fill}"/>'
                f'<w:tcMar>'
                f'<w:top w:w="120" w:type="dxa"/>'
                f'<w:bottom w:w="120" w:type="dxa"/>'
                f'<w:left w:w="160" w:type="dxa"/>'
                f'<w:right w:w="160" w:type="dxa"/>'
                f'</w:tcMar>'
                f'</w:tcPr>'
            )
            run = make_run(cell.strip(), bold=is_h, color=text_color, size=20 if is_h else 19, font="Arial")
            tbl.append(make_para(run, align="center" if is_h else "left", space_before=40, space_after=40))
            tbl.append('</w:tc>')
        tbl.append('</w:tr>')
    tbl.append('</w:tbl>')
    return "".join(tbl)

def make_screenshot_placeholder(fig_title, fig_num):
    tbl = ['<w:tbl>']
    tbl.append(
        '<w:tblPr>'
        '<w:tblW w:w="9360" w:type="dxa"/>'
        '<w:jc w:val="center"/>'
        '<w:tblBorders>'
        '<w:top w:val="dashed" w:sz="8" w:space="0" w:color="94A3B8"/>'
        '<w:left w:val="dashed" w:sz="8" w:space="0" w:color="94A3B8"/>'
        '<w:bottom w:val="dashed" w:sz="8" w:space="0" w:color="94A3B8"/>'
        '<w:right w:val="dashed" w:sz="8" w:space="0" w:color="94A3B8"/>'
        '</w:tblBorders>'
        '</w:tblPr>'
    )
    tbl.append('<w:tr>')
    tbl.append(
        '<w:trPr>'
        '<w:trHeight w:val="2800" w:hRule="atLeast"/>'
        '</w:trPr>'
    )
    tbl.append(
        '<w:tc>'
        '<w:tcPr>'
        '<w:shd w:val="clear" w:color="auto" w:fill="F8FAFC"/>'
        '<w:vAlign w:val="center"/>'
        '<w:tcMar><w:top w:w="300" w:type="dxa"/><w:bottom w:w="300" w:type="dxa"/><w:left w:w="300" w:type="dxa"/><w:right w:w="300" w:type="dxa"/></w:tcMar>'
        '</w:tcPr>'
    )
    
    r1 = make_run(f"[ PASTE SCREENSHOT HERE: FIGURE {fig_num} ]", bold=True, color="2563EB", size=24, font="Arial")
    p1 = make_para(r1, align="center", space_before=600, space_after=100)
    r2 = make_run(fig_title, italic=True, color="64748B", size=20, font="Arial")
    p2 = make_para(r2, align="center", space_before=50, space_after=600)
    
    tbl.append(p1 + p2)
    tbl.append('</w:tc>')
    tbl.append('</w:tr>')
    tbl.append('</w:tbl>')
    
    caption_run = make_run(f"Figure {fig_num}: ", bold=True, color="0F172A", size=20, font="Arial") + make_run(fig_title, italic=True, color="334155", size=20, font="Arial")
    caption_p = make_para(caption_run, align="center", space_before=100, space_after=240)
    return "".join(tbl) + caption_p

def parse_markdown_to_xml(md_content):
    lines = md_content.split("\n")
    body_elements = []
    
    in_code = False
    code_lines = []
    code_lang = ""
    
    in_table = False
    table_rows = []
    
    in_box = False
    box_lines = []
    
    i = 0
    fig_counter = 1
    
    while i < len(lines):
        line = lines[i]
        
        # Check page breaks
        if "page-break-after: always" in line or line.strip() == "---" and i < 80:
            if in_table:
                body_elements.append(make_table(table_rows))
                table_rows = []
                in_table = False
            body_elements.append(make_page_break())
            i += 1
            continue
            
        # Check code block toggle
        if line.strip().startswith("```"):
            if not in_code:
                in_code = True
                code_lang = line.strip()[3:].strip()
                code_lines = []
            else:
                in_code = False
                # Check if it was a screenshot placeholder box
                full_code = "\n".join(code_lines)
                if "[ PASTE SCREENSHOT HERE" in full_code:
                    # extract title
                    m = re.search(r"\[ PASTE SCREENSHOT HERE: FIGURE (\d+) \]\s*\n\s*(.*?)\s*(\n|$)", full_code)
                    if m:
                        fnum = m.group(1)
                        ftitle = m.group(2).strip()
                    else:
                        fnum = str(fig_counter)
                        ftitle = f"Interface Screenshot {fig_counter}"
                    fig_counter += 1
                    body_elements.append(make_screenshot_placeholder(ftitle, fnum))
                else:
                    body_elements.append(make_code_block(code_lines, code_lang))
                code_lines = []
            i += 1
            continue
            
        if in_code:
            code_lines.append(line)
            i += 1
            continue
            
        # Table parsing
        if line.strip().startswith("|") and line.strip().endswith("|"):
            cells = [c.strip() for c in line.strip().split("|")[1:-1]]
            # ignore separator line | :--- | :--- |
            if all(re.match(r"^:?-+:?$", c) for c in cells):
                i += 1
                continue
            table_rows.append(cells)
            in_table = True
            i += 1
            continue
        else:
            if in_table:
                body_elements.append(make_table(table_rows))
                table_rows = []
                in_table = False
                
        # Markdown Headings
        if line.startswith("# "):
            text = line[2:].strip()
            # Title
            r = make_run(text, bold=True, color="0F172A", size=44, font="Arial")
            body_elements.append(make_para(r, align="center", space_before=240, space_after=160))
            i += 1
            continue
        elif line.startswith("## "):
            text = line[3:].strip()
            body_elements.append(make_heading(text, level=1))
            i += 1
            continue
        elif line.startswith("### "):
            text = line[4:].strip()
            body_elements.append(make_heading(text, level=2))
            i += 1
            continue
        elif line.startswith("#### "):
            text = line[5:].strip()
            body_elements.append(make_heading(text, level=3))
            i += 1
            continue
            
        # Bullet list
        if line.strip().startswith("- ") or line.strip().startswith("* "):
            item_text = line.strip()[2:].strip()
            # parse bold inside
            runs = []
            parts = re.split(r"(\*\*.*?\*\*)", item_text)
            for part in parts:
                if part.startswith("**") and part.endswith("**"):
                    runs.append(make_run(part[2:-2], bold=True, color="1E293B", size=22))
                else:
                    runs.append(make_run(part, bold=False, color="334155", size=22))
            body_elements.append(make_para("".join(runs), align="left", space_before=40, space_after=40))
            i += 1
            continue
            
        # Numbered list
        m_num = re.match(r"^(\d+)\.\s+(.*)$", line.strip())
        if m_num:
            prefix = m_num.group(1) + ". "
            item_text = m_num.group(2)
            runs = [make_run(prefix, bold=True, color="2563EB", size=22)]
            parts = re.split(r"(\*\*.*?\*\*)", item_text)
            for part in parts:
                if part.startswith("**") and part.endswith("**"):
                    runs.append(make_run(part[2:-2], bold=True, color="1E293B", size=22))
                else:
                    runs.append(make_run(part, bold=False, color="334155", size=22))
            body_elements.append(make_para("".join(runs), align="left", space_before=60, space_after=60))
            i += 1
            continue
            
        # Ignore Figure bold paragraph beneath screenshot if already emitted
        if line.strip().startswith("**Figure") and "PASTE SCREENSHOT HERE" in "".join(lines[max(0, i-6):i]):
            i += 1
            continue
            
        # Blank line
        if not line.strip():
            i += 1
            continue
            
        # Standard paragraph
        parts = re.split(r"(\*\*.*?\*\*|\*.*?\*|`.*?`)", line.strip())
        runs = []
        for part in parts:
            if part.startswith("**") and part.endswith("**"):
                runs.append(make_run(part[2:-2], bold=True, color="0F172A", size=22))
            elif part.startswith("*") and part.endswith("*"):
                runs.append(make_run(part[1:-1], italic=True, color="334155", size=22))
            elif part.startswith("`") and part.endswith("`"):
                runs.append(make_run(part[1:-1], bold=False, color="1D4ED8", size=20, font="Consolas"))
            else:
                runs.append(make_run(part, bold=False, color="334155", size=22))
                
        align = "center" if "Academic Year" in line or "Department of" in line or "FEDERAL INSTITUTE" in line or "Course:" in line or "Under the guidance" in line else "left"
        body_elements.append(make_para("".join(runs), align=align, space_before=60, space_after=80))
        i += 1
        
    if in_table:
        body_elements.append(make_table(table_rows))
        
    return "\n".join(body_elements)

def build_docx(md_path, output_docx_path):
    with open(md_path, "r", encoding="utf-8") as f:
        md_text = f.read()
        
    body_xml = parse_markdown_to_xml(md_text)
    
    document_xml = f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
            xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <w:body>
{body_xml}
    <w:sectPr>
      <w:pgSz w:w="12240" w:h="15840"/>
      <w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="720" w:footer="720" w:gutter="0"/>
    </w:sectPr>
  </w:body>
</w:document>'''

    content_types = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
  <Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
  <Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
</Types>'''

    rels = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
</Relationships>'''

    document_rels = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>'''

    core_props = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <dc:title>MICRO PROJECT REPORT - VEHICLE RENTAL MANAGEMENT SYSTEM</dc:title>
  <dc:subject>Object-Oriented Programming Micro Project</dc:subject>
  <dc:creator>FISAT Students</dc:creator>
</cp:coreProperties>'''

    app_props = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties">
  <Application>Microsoft Office Word</Application>
</Properties>'''

    styles_xml = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:docDefaults>
    <w:rPrDefault>
      <w:rPr>
        <w:rFonts w:ascii="Arial" w:eastAsia="Arial" w:hAnsi="Arial" w:cs="Arial"/>
        <w:sz w:val="22"/>
        <w:color w:val="1E293B"/>
      </w:rPr>
    </w:rPrDefault>
  </w:docDefaults>
</w:styles>'''

    with zipfile.ZipFile(output_docx_path, 'w', zipfile.ZIP_DEFLATED) as docx:
        docx.writestr('[Content_Types].xml', content_types)
        docx.writestr('_rels/.rels', rels)
        docx.writestr('word/_rels/document.xml.rels', document_rels)
        docx.writestr('word/document.xml', document_xml)
        docx.writestr('word/styles.xml', styles_xml)
        docx.writestr('docProps/core.xml', core_props)
        docx.writestr('docProps/app.xml', app_props)

    print(f"Successfully generated DOCX at: {output_docx_path}")

if __name__ == "__main__":
    src = "/Users/rohan_rajesh/Desktop/OOPS/MICRO_PROJECT_REPORT.md"
    dst = "/Users/rohan_rajesh/Desktop/OOPS/MICRO_PROJECT_REPORT.docx"
    build_docx(src, dst)
