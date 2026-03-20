import os
from reportlab.lib.pagesizes import letter, landscape
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, PageBreak
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib import colors
from reportlab.graphics.shapes import Drawing, Rect, String, Line, PolyLine, Group, Polygon

def draw_architecture():
    # Use drawing of 700 width, 450 height
    d = Drawing(700, 450)
    
    def box(cx, cy, w, h, text, text2="", fill=colors.blue):
        g = Group()
        x = cx - w/2
        y = cy - h/2
        g.add(Rect(x, y, w, h, fillColor=fill, strokeColor=colors.black, rx=4, ry=4))
        if text2:
            g.add(String(cx, cy + 2, text, textAnchor='middle', fontName='Helvetica-Bold', fontSize=10, fillColor=colors.white))
            g.add(String(cx, cy - h/4 - 2, text2, textAnchor='middle', fontName='Helvetica', fontSize=8, fillColor=colors.white))
        else:
            g.add(String(cx, cy - 3, text, textAnchor='middle', fontName='Helvetica-Bold', fontSize=10, fillColor=colors.white))
        return g

    def route_down(x1, y1, x2, y2, color=colors.HexColor("#333333"), label=""):
        g = Group()
        mid_y = (y1 + y2)/2
        g.add(PolyLine([x1, y1, x1, mid_y, x2, mid_y, x2, y2], strokeColor=color, strokeWidth=1.2))
        g.add(Polygon([x2, y2, x2-3, y2+6, x2+3, y2+6], fillColor=color, strokeColor=color))
        if label:
            g.add(String((x1+x2)/2, mid_y + 3, label, textAnchor='middle', fontSize=7, fillColor=color))
        return g

    def route_across(x1, y1, x2, y2, color=colors.red, dash=None, label=""):
        g = Group()
        mid_x = (x1 + x2)/2
        shape = PolyLine([x1, y1, mid_x, y1, mid_x, y2, x2, y2], strokeColor=color, strokeWidth=1.5)
        if dash:
            shape.strokeDashArray = dash
        g.add(shape)
        if x2 > x1:
            g.add(Polygon([x2, y2, x2-6, y2+3, x2-6, y2-3], fillColor=color, strokeColor=color))
        else:
            g.add(Polygon([x2, y2, x2+6, y2+3, x2+6, y2-3], fillColor=color, strokeColor=color))
        if label:
            g.add(String(mid_x, (y1+y2)/2 + 3, label, textAnchor='middle', fontSize=8, fillColor=color))
        return g

    # Layer Backgrounds
    d.add(Rect(10, 390, 680, 55, fillColor=colors.HexColor("#f4f9f9"), strokeColor=colors.transparent)) 
    d.add(String(20, 430, "1. Web Layer", fontName='Helvetica-Bold', fontSize=10, fillColor=colors.gray))

    d.add(Rect(10, 290, 680, 75, fillColor=colors.HexColor("#e8f1fa"), strokeColor=colors.transparent))
    d.add(String(20, 350, "2. API Routing & Discovery Layer", fontName='Helvetica-Bold', fontSize=10, fillColor=colors.gray))

    d.add(Rect(10, 160, 680, 100, fillColor=colors.HexColor("#dcedf7"), strokeColor=colors.transparent))
    d.add(String(20, 245, "3. Microservices Core Layer", fontName='Helvetica-Bold', fontSize=10, fillColor=colors.gray))

    d.add(Rect(10, 20, 680, 100, fillColor=colors.HexColor("#d5e2ec"), strokeColor=colors.transparent)) 
    d.add(String(20, 105, "4. Database Tier", fontName='Helvetica-Bold', fontSize=10, fillColor=colors.gray))

    # Applications
    d.add(box(350, 415, 200, 35, "Angular Web Frontend", "localhost:4200", colors.HexColor("#2c4d82")))
    
    d.add(box(350, 325, 200, 35, "Spring Cloud API Gateway", "Routing (:8080)", colors.HexColor("#4674b8")))
    d.add(box(550, 325, 120, 30, "Eureka Registry", "Service Map (:8761)", colors.HexColor("#25a69a")))

    d.add(route_down(350, 397, 350, 342, label="REST HTTP Requests"))
    d.add(route_across(450, 325, 490, 325, colors.gray, dash=[2,2], label="Registers IP"))

    srv = [
        ("Auth", 90, 8081, colors.HexColor("#7a4f9a")),
        ("Booking", 195, 8082, colors.HexColor("#d35400")),
        ("History", 300, 8083, colors.HexColor("#c0392b")),
        ("Payment", 405, 8085, colors.HexColor("#27ae60")),
        ("Tracking", 510, 8086, colors.HexColor("#2980b9")),
        ("Invoice", 615, 8087, colors.HexColor("#e67e22")),
    ]

    for name, cx, port, col in srv:
        d.add(box(cx, 210, 85, 40, f"{name} Service", f"Port: {port}", col))
        d.add(route_down(350, 307, cx, 230, colors.gray))

    # Cross-service calls
    d.add(route_across(447, 215, 572, 215, colors.red, label="Sync HTTP Post (RestTemplate)"))
    d.add(route_across(447, 205, 467, 205, colors.red)) 

    # DB Layer
    d.add(box(195, 70, 220, 50, "Shared DB (H2 File)", "~/psm_shared_db (Persistent)", colors.HexColor("#34495e")))
    d.add(box(510, 70, 260, 50, "In-Memory H2 DBs", "Auth, Payment, Tracking, Invoice (Volatile)", colors.HexColor("#7f8c8d")))

    d.add(route_down(195, 190, 160, 95))
    d.add(route_down(300, 190, 230, 95))
    d.add(route_down(90, 190, 420, 95))
    d.add(route_down(405, 190, 480, 95))
    d.add(route_down(510, 190, 540, 95))
    d.add(route_down(615, 190, 600, 95))
    
    return d

def generate_detailed_pdf():
    doc = SimpleDocTemplate(
        "C:/Users/HARSH/Desktop/SpringBoot/PSM/docs/PSM_Interactive_Architecture_Guide.pdf",
        pagesize=landscape(letter),
        rightMargin=30, leftMargin=30, topMargin=30, bottomMargin=30
    )
    styles = getSampleStyleSheet()
    
    title_style = ParagraphStyle('Title', parent=styles['Heading1'], fontSize=20, spaceAfter=20, alignment=1)
    h1 = ParagraphStyle('H1', parent=styles['Heading2'], fontSize=16, spaceAfter=10, textColor=colors.HexColor("#2c4d82"))
    normal = ParagraphStyle('Normal', parent=styles['Normal'], fontSize=11, spaceAfter=8)

    elements = []

    elements.append(Paragraph("Postal Service Management (PSM) - Professional Visual Architecture & Interaction Flow", title_style))
    elements.append(Spacer(1, 10))
    
    elements.append(Paragraph("1. System Architecture Diagram & Database Interactions", h1))
    elements.append(Paragraph("This architecture map plots exact HTTP mappings. Watch how Angular hits the Gateway, routes downstream via Eureka discovery, triggers isolated service DB stores, and utilizes RestTemplate for Inter-Service dependencies.", normal))
    elements.append(Spacer(1, 10))
    
    elements.append(draw_architecture())
    elements.append(PageBreak())

    elements.append(Paragraph("2. Detailed Sequence Flow: Customer Journey (Registration to Tracking)", h1))
    elements.append(Paragraph("The exact request flow sequence across multiple independent controllers as the user traverses the postal features, showing how databases are updated and client interactions processed.", normal))
    
    def draw_sequence_user():
        d = Drawing(700, 430)
        
        def ll(cx, name):
            g = Group()
            g.add(Rect(cx-40, 400, 80, 25, fillColor=colors.HexColor("#34495e"), strokeColor=colors.black, rx=3,ry=3))
            g.add(String(cx, 408, name, textAnchor='middle', fontName='Helvetica-Bold', fontSize=9, fillColor=colors.white))
            g.add(Line(cx, 400, cx, 10, strokeColor=colors.gray, strokeDashArray=[3,3]))
            return g
        
        nodes = [(60, "Frontend UI"), (170, "Gateway"), (280, "Auth SRV"), (390, "Booking SRV"), (500, "Payment SRV"), (610, "Other SRV's")]
        for cx, name in nodes: d.add(ll(cx, name))

        def arrow(x1, x2, y, text, is_return=False, note=""):
            g = Group()
            col = colors.green if is_return else colors.HexColor("#2980b9")
            shape = Line(x1, y, x2, y, strokeColor=col, strokeWidth=1.5)
            if is_return: shape.strokeDashArray = [4,4]
            g.add(shape)
            
            if x2 > x1: g.add(Polygon([x2, y, x2-5, y+3, x2-5, y-3], fillColor=col, strokeColor=col))
            else: g.add(Polygon([x2, y, x2+5, y+3, x2+5, y-3], fillColor=col, strokeColor=col))
            
            g.add(String((x1+x2)/2, y+4, text, textAnchor='middle', fontSize=8, fillColor=colors.black))
            if note: g.add(String((x1+x2)/2, y-8, note, textAnchor='middle', fontSize=7, fillColor=colors.red))
            return g

        seqs = [
            (60, 170, "1. POST /api/auth/register", False, ""),
            (170, 280, "2. Route Request to Auth", False, ""),
            (280, 280, "", False, "=> Insert into Auth In-Mem DB"),
            (280, 170, "3. Token Return", True, ""),
            (170, 60, "4. Save Bearer Auth Context", True, ""),
            (60, 170, "5. POST /api/booking", False, ""),
            (170, 390, "6. Validate Token via Gateway", False, ""),
            (390, 390, "", False, "=> Insert into Shared H2 DB"),
            (390, 170, "7. Booking 'Created' 201", True, ""),
            (170, 60, "8. Pass Booking ID context to Payment UI", True, ""),
            (60, 170, "9. POST /api/payment/create", False, ""),
            (170, 500, "10. Proxy Payment Request", False, ""),
            (500, 610, "11. RestTemplate Call to Sync Invoice", False, ""),
            (500, 610, "12. RestTemplate Call to Sync Tracking", False, ""),
            (610, 500, "13. Acknowledged", True, ""),
            (500, 500, "", False, "=> Insert into Payment In-Mem DB"),
            (500, 170, "14. Payment Confirmed", True, ""),
            (170, 60, "15. UI Shows Tracking Path", True, ""),
        ]
        
        y = 380
        for ax1, ax2, txt, ret, nt in seqs:
            if "Insert" in nt:
                d.add(String(ax1, y-5, nt, textAnchor='middle', fontName='Helvetica-Oblique', fontSize=7, fillColor=colors.red))
                y -= 10
            else:
                d.add(arrow(ax1, ax2, y, txt, ret))
                y -= 20
        return d
    
    elements.append(draw_sequence_user())
    elements.append(PageBreak())

    elements.append(Paragraph("3. Detailed Sequence Flow: Internal Officer Operations", h1))
    elements.append(Paragraph("A clean trace of logistics workflows. An authenticated corporate Officer alters database states and these exact changes propagate automatically down to the client applications via microservice responses.", normal))
    
    def draw_sequence_officer():
        d = Drawing(700, 400)
        def ll(cx, name):
            g = Group()
            g.add(Rect(cx-40, 380, 80, 25, fillColor=colors.HexColor("#e67e22"), strokeColor=colors.black, rx=3,ry=3))
            g.add(String(cx, 388, name, textAnchor='middle', fontName='Helvetica-Bold', fontSize=9, fillColor=colors.white))
            g.add(Line(cx, 380, cx, 10, strokeColor=colors.gray, strokeDashArray=[3,3]))
            return g
        
        nodes = [(60, "Officer App View"), (180, "Gateway"), (300, "Tracking SRV"), (420, "Shared Databases"), (540, "History SRV"), (660, "Customer UX")]
        for cx, name in nodes: d.add(ll(cx, name))

        def arrow(x1, x2, y, text, is_return=False):
            g = Group()
            col = colors.green if is_return else colors.HexColor("#8e44ad")
            shape = Line(x1, y, x2, y, strokeColor=col, strokeWidth=1.5)
            if is_return: shape.strokeDashArray = [4,4]
            g.add(shape)
            if x2 > x1: g.add(Polygon([x2, y, x2-5, y+3, x2-5, y-3], fillColor=col, strokeColor=col))
            else: g.add(Polygon([x2, y, x2+5, y+3, x2+5, y-3], fillColor=col, strokeColor=col))
            g.add(String((x1+x2)/2, y+4, text, textAnchor='middle', fontSize=8, fillColor=colors.black))
            return g

        seqs = [
            (60, 180, "1. Access Dashboard GET /tracking/officer/shipments"),
            (180, 300, "2. Request Routed"),
            (300, 420, "3. Extract active statuses from DB"),
            (420, 300, "4. Return Dataset rows", True),
            (300, 180, "5. Returns Payload", True),
            (180, 60, "6. Hydrate Table Data on Frontend", True),
            (60, 180, "7. Select Record & PUT status (e.g. IN_TRANSIT)"),
            (180, 300, "8. Validate Role == 'OFFICER' at proxy/endpoint"),
            (300, 420, "9. Overwrite new state onto DB entity"),
            (420, 300, "10. Commit Confirmed", True),
            (300, 180, "11. Dispatch HTTP Success", True),
            (180, 60, "12. UI Notification to Officer", True),
            (660, 180, "13. End-User refreshes GET /api/history"),
            (180, 540, "14. Proxy to History SRV"),
            (540, 420, "15. DB join / Fetch shared record"),
            (420, 540, "16. Yields new IN_TRANSIT state", True),
            (540, 180, "17. History JSON returned", True),
            (180, 660, "18. Display updated state for Customer", True)
        ]
        
        y = 360
        for ax1, ax2, txt, *ret in seqs:
            is_ret = ret[0] if ret else False
            d.add(arrow(ax1, ax2, y, txt, is_ret))
            y -= 19
        return d
        
    elements.append(draw_sequence_officer())

    doc.build(elements)
    print("New Visual Architecture PDF Generation Complete: docs/PSM_Interactive_Architecture_Guide.pdf")

if __name__ == "__main__":
    generate_detailed_pdf()
