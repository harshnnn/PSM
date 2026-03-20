import base64
import datetime
import io
import re
import urllib.error
import urllib.request
from pathlib import Path

from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.platypus import (
    Image,
    PageBreak,
    Paragraph,
    Preformatted,
    SimpleDocTemplate,
    Spacer,
)

ROOT = Path(__file__).resolve().parent
SOURCE_MD = ROOT / "PSM_Microservices_Detailed_Flow.md"
OUTPUT_PDF = ROOT / "PSM_Microservices_Detailed_Flow.pdf"


def fetch_mermaid_png(mermaid_text: str) -> bytes | None:
    """Render Mermaid diagram to PNG using Kroki public API."""
    data = mermaid_text.encode("utf-8")
    req = urllib.request.Request(
        url="https://kroki.io/mermaid/png",
        data=data,
        headers={"Content-Type": "text/plain", "Accept": "image/png"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(req, timeout=20) as resp:
            if resp.status != 200:
                return None
            return resp.read()
    except (urllib.error.URLError, TimeoutError, OSError):
        return None


def parse_markdown_blocks(md_text: str):
    """Very small markdown parser for headings, bullets, text and fenced code blocks."""
    lines = md_text.splitlines()
    blocks = []
    i = 0

    while i < len(lines):
        line = lines[i]

        if line.startswith("```"):
            lang = line[3:].strip().lower()
            code_lines = []
            i += 1
            while i < len(lines) and not lines[i].startswith("```"):
                code_lines.append(lines[i])
                i += 1
            blocks.append(("code", lang, "\n".join(code_lines).strip()))
            i += 1
            continue

        if line.startswith("# "):
            blocks.append(("h1", line[2:].strip()))
            i += 1
            continue

        if line.startswith("## "):
            blocks.append(("h2", line[3:].strip()))
            i += 1
            continue

        if line.startswith("### "):
            blocks.append(("h3", line[4:].strip()))
            i += 1
            continue

        if line.startswith("- "):
            bullets = []
            while i < len(lines) and lines[i].startswith("- "):
                bullets.append(lines[i][2:].strip())
                i += 1
            blocks.append(("bullets", bullets))
            continue

        if line.strip() == "":
            blocks.append(("space",))
            i += 1
            continue

        para_lines = [line]
        i += 1
        while i < len(lines):
            nxt = lines[i]
            if (
                nxt.strip() == ""
                or nxt.startswith("#")
                or nxt.startswith("- ")
                or nxt.startswith("```")
            ):
                break
            para_lines.append(nxt)
            i += 1
        blocks.append(("p", " ".join(p.strip() for p in para_lines).strip()))

    return blocks


def make_styles():
    base = getSampleStyleSheet()
    styles = {
        "title": ParagraphStyle(
            "TitleCustom",
            parent=base["Heading1"],
            fontSize=22,
            leading=26,
            textColor=colors.HexColor("#113355"),
            spaceAfter=8,
        ),
        "subtitle": ParagraphStyle(
            "SubCustom",
            parent=base["Normal"],
            fontSize=11,
            textColor=colors.HexColor("#4a6178"),
            leading=14,
            spaceAfter=14,
        ),
        "h2": ParagraphStyle(
            "H2Custom",
            parent=base["Heading2"],
            fontSize=15,
            leading=19,
            textColor=colors.HexColor("#1f4f82"),
            spaceBefore=8,
            spaceAfter=8,
        ),
        "h3": ParagraphStyle(
            "H3Custom",
            parent=base["Heading3"],
            fontSize=12,
            leading=15,
            textColor=colors.HexColor("#2f5f72"),
            spaceBefore=6,
            spaceAfter=6,
        ),
        "body": ParagraphStyle(
            "BodyCustom",
            parent=base["Normal"],
            fontSize=10.5,
            leading=14,
            textColor=colors.HexColor("#222222"),
            spaceAfter=5,
        ),
        "bullet": ParagraphStyle(
            "BulletCustom",
            parent=base["Normal"],
            fontSize=10.5,
            leading=14,
            leftIndent=12,
            bulletIndent=2,
            textColor=colors.HexColor("#222222"),
            spaceAfter=3,
        ),
        "code": ParagraphStyle(
            "CodeCustom",
            parent=base["Code"],
            fontName="Courier",
            fontSize=8.4,
            leading=10,
            backColor=colors.HexColor("#f5f7fb"),
            borderColor=colors.HexColor("#d8deea"),
            borderWidth=0.5,
            borderPadding=5,
            textColor=colors.HexColor("#1f2a37"),
            spaceBefore=3,
            spaceAfter=8,
        ),
    }
    return styles


def build_story(md_text: str):
    styles = make_styles()
    story = []

    blocks = parse_markdown_blocks(md_text)
    title_used = False

    for block in blocks:
        kind = block[0]

        if kind == "h1":
            if not title_used:
                story.append(Paragraph(block[1], styles["title"]))
                story.append(
                    Paragraph(
                        f"Professional service-level architecture and data-flow documentation. Generated on {datetime.datetime.now().strftime('%d %b %Y %H:%M')}",
                        styles["subtitle"],
                    )
                )
                story.append(Spacer(1, 4 * mm))
                title_used = True
            else:
                story.append(PageBreak())
                story.append(Paragraph(block[1], styles["title"]))
                story.append(Spacer(1, 2 * mm))
            continue

        if kind == "h2":
            story.append(Paragraph(block[1], styles["h2"]))
            continue

        if kind == "h3":
            story.append(Paragraph(block[1], styles["h3"]))
            continue

        if kind == "p":
            txt = block[1]
            txt = re.sub(r"`([^`]+)`", r"<font name='Courier'>\1</font>", txt)
            story.append(Paragraph(txt, styles["body"]))
            continue

        if kind == "bullets":
            for item in block[1]:
                item = re.sub(r"`([^`]+)`", r"<font name='Courier'>\1</font>", item)
                story.append(Paragraph(item, styles["bullet"], bulletText="•"))
            story.append(Spacer(1, 1.6 * mm))
            continue

        if kind == "space":
            story.append(Spacer(1, 1.2 * mm))
            continue

        if kind == "code":
            lang = block[1]
            code = block[2]
            if not code:
                continue

            if lang == "mermaid":
                png_bytes = fetch_mermaid_png(code)
                if png_bytes:
                    img = Image(io.BytesIO(png_bytes))
                    max_w = 175 * mm
                    max_h = 92 * mm
                    ratio = min(max_w / img.drawWidth, max_h / img.drawHeight)
                    img.drawWidth *= ratio
                    img.drawHeight *= ratio
                    story.append(img)
                    story.append(Spacer(1, 2 * mm))
                else:
                    # Fallback if network render unavailable
                    story.append(Paragraph("Diagram (render unavailable in this environment):", styles["body"]))
                    story.append(Preformatted(code, styles["code"]))
            else:
                story.append(Preformatted(code, styles["code"]))
            continue

    return story


def add_page_number(canvas, doc):
    canvas.saveState()
    canvas.setFont("Helvetica", 8)
    canvas.setFillColor(colors.HexColor("#6b7280"))
    canvas.drawRightString(200 * mm, 8 * mm, f"Page {doc.page}")
    canvas.restoreState()


def main():
    if not SOURCE_MD.exists():
        raise FileNotFoundError(f"Source markdown not found: {SOURCE_MD}")

    md_text = SOURCE_MD.read_text(encoding="utf-8")
    story = build_story(md_text)

    doc = SimpleDocTemplate(
        str(OUTPUT_PDF),
        pagesize=A4,
        leftMargin=14 * mm,
        rightMargin=14 * mm,
        topMargin=14 * mm,
        bottomMargin=14 * mm,
        title="PSM Microservices Detailed Flow",
        author="GitHub Copilot",
    )

    doc.build(story, onFirstPage=add_page_number, onLaterPages=add_page_number)
    print(f"Generated: {OUTPUT_PDF}")


if __name__ == "__main__":
    main()
