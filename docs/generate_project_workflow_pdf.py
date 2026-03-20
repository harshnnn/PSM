from reportlab.lib.pagesizes import A4
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether
from reportlab.graphics.shapes import Drawing, Rect, String, Line, Polygon
from reportlab.graphics import renderPDF


OUTPUT_PATH = r"c:\Users\HARSH\Desktop\SpringBoot\PSM\docs\PSM_Project_Workflow_Guide.pdf"


def heading(text, level=1):
    styles = getSampleStyleSheet()
    if level == 1:
        style = ParagraphStyle(
            "h1_custom",
            parent=styles["Heading1"],
            fontName="Helvetica-Bold",
            fontSize=18,
            leading=22,
            textColor=colors.HexColor("#0B3D2E"),
            spaceAfter=8,
        )
    elif level == 2:
        style = ParagraphStyle(
            "h2_custom",
            parent=styles["Heading2"],
            fontName="Helvetica-Bold",
            fontSize=14,
            leading=18,
            textColor=colors.HexColor("#0E6251"),
            spaceBefore=6,
            spaceAfter=6,
        )
    else:
        style = ParagraphStyle(
            "h3_custom",
            parent=styles["Heading3"],
            fontName="Helvetica-Bold",
            fontSize=12,
            leading=15,
            textColor=colors.HexColor("#117A65"),
            spaceBefore=4,
            spaceAfter=4,
        )
    return Paragraph(text, style)


def body(text):
    styles = getSampleStyleSheet()
    style = ParagraphStyle(
        "body_custom",
        parent=styles["BodyText"],
        fontName="Helvetica",
        fontSize=10.5,
        leading=14,
        textColor=colors.HexColor("#1F2933"),
    )
    return Paragraph(text, style)


def bullet(text):
    return body(f"- {text}")


def build_architecture_diagram():
    d = Drawing(520, 340)

    def box(x, y, w, h, title, subtitle, fill):
        d.add(Rect(x, y, w, h, fillColor=fill, strokeColor=colors.HexColor("#1F2933"), strokeWidth=1))
        d.add(String(x + 6, y + h - 16, title, fontName="Helvetica-Bold", fontSize=9, fillColor=colors.white))
        d.add(String(x + 6, y + h - 30, subtitle, fontName="Helvetica", fontSize=7.5, fillColor=colors.white))

    # Top row
    box(20, 275, 150, 45, "Angular Frontend", "localhost:4200", colors.HexColor("#1D4E89"))
    box(190, 275, 150, 45, "API Gateway", "localhost:8080", colors.HexColor("#0E7490"))
    box(360, 275, 140, 45, "Service Registry", "Eureka :8761", colors.HexColor("#0F766E"))

    # Middle service row
    box(20, 200, 115, 45, "Auth Service", ":8081", colors.HexColor("#7C3AED"))
    box(145, 200, 115, 45, "Booking Service", ":8082", colors.HexColor("#059669"))
    box(270, 200, 115, 45, "Payment Service", ":8085", colors.HexColor("#B45309"))
    box(395, 200, 105, 45, "Invoice Service", ":8087", colors.HexColor("#C2410C"))

    # Bottom row
    box(85, 125, 150, 45, "Booking History Service", ":8083", colors.HexColor("#7C2D12"))
    box(285, 125, 150, 45, "Tracking Service", ":8086", colors.HexColor("#1F6FEB"))

    # DB row
    box(85, 45, 150, 45, "Shared H2 File DB", "~/psm_shared_db.mv.db", colors.HexColor("#374151"))
    box(285, 45, 150, 45, "In-Memory H2 DBs", "auth/payment/invoice/tracking", colors.HexColor("#4B5563"))

    # Arrows helper
    def arrow(x1, y1, x2, y2):
        d.add(Line(x1, y1, x2, y2, strokeColor=colors.HexColor("#111827"), strokeWidth=1))
        # simple arrow head
        dx = x2 - x1
        dy = y2 - y1
        if dx == 0 and dy == 0:
            return
        if abs(dx) > abs(dy):
            if dx > 0:
                pts = [x2, y2, x2 - 6, y2 + 3, x2 - 6, y2 - 3]
            else:
                pts = [x2, y2, x2 + 6, y2 + 3, x2 + 6, y2 - 3]
        else:
            if dy > 0:
                pts = [x2, y2, x2 - 3, y2 - 6, x2 + 3, y2 - 6]
            else:
                pts = [x2, y2, x2 - 3, y2 + 6, x2 + 3, y2 + 6]
        d.add(Polygon(pts, fillColor=colors.HexColor("#111827"), strokeColor=colors.HexColor("#111827")))

    # Flows
    arrow(170, 297, 190, 297)   # frontend -> gateway
    arrow(265, 275, 265, 245)   # gateway down
    arrow(240, 275, 77, 245)    # gateway -> auth
    arrow(240, 275, 202, 245)   # gateway -> booking
    arrow(240, 275, 327, 245)   # gateway -> payment
    arrow(240, 275, 445, 245)   # gateway -> invoice
    arrow(240, 275, 160, 170)   # gateway -> history
    arrow(240, 275, 360, 170)   # gateway -> tracking

    # registration to eureka
    arrow(430, 275, 80, 245)
    arrow(430, 275, 200, 245)
    arrow(430, 275, 325, 245)
    arrow(430, 275, 445, 245)
    arrow(430, 275, 160, 170)
    arrow(430, 275, 360, 170)

    # inter-service calls
    arrow(327, 222, 202, 222)   # payment -> booking
    arrow(327, 210, 445, 210)   # payment -> invoice
    arrow(327, 200, 360, 170)   # payment -> tracking
    arrow(360, 145, 202, 222)   # tracking -> booking
    arrow(160, 145, 360, 145)   # history -> tracking

    # db links
    arrow(202, 200, 160, 90)    # booking -> shared
    arrow(160, 125, 160, 90)    # history -> shared
    arrow(80, 200, 360, 90)     # auth -> mem group
    arrow(325, 200, 360, 90)    # payment -> mem group
    arrow(445, 200, 360, 90)    # invoice -> mem group
    arrow(360, 125, 360, 90)    # tracking -> mem group

    return d


def build_sequence_diagram_payment_flow():
    d = Drawing(520, 310)

    actors = [
        (20, "User"),
        (100, "Frontend"),
        (190, "Gateway"),
        (280, "Payment"),
        (360, "Booking"),
        (440, "Invoice"),
    ]

    for x, name in actors:
        d.add(Rect(x, 275, 70, 24, fillColor=colors.HexColor("#0F766E"), strokeColor=colors.HexColor("#0F172A"), strokeWidth=1))
        d.add(String(x + 8, 283, name, fontName="Helvetica-Bold", fontSize=8, fillColor=colors.white))
        d.add(Line(x + 35, 30, x + 35, 275, strokeColor=colors.HexColor("#94A3B8"), strokeWidth=0.8))

    def msg(x1, x2, y, label):
        d.add(Line(x1, y, x2, y, strokeColor=colors.HexColor("#1E293B"), strokeWidth=1))
        d.add(Polygon([x2, y, x2 - 6, y + 3, x2 - 6, y - 3], fillColor=colors.HexColor("#1E293B"), strokeColor=colors.HexColor("#1E293B")))
        d.add(String(min(x1, x2) + 3, y + 4, label, fontName="Helvetica", fontSize=7, fillColor=colors.HexColor("#111827")))

    # Payment flow
    msg(55, 135, 245, "Click Pay")
    msg(135, 225, 225, "POST /api/payments")
    msg(225, 315, 205, "Route by /api/payments/**")
    msg(315, 395, 185, "GET /api/bookings/{id}")
    msg(315, 475, 165, "POST /api/invoices")
    msg(315, 395, 145, "PUT /api/bookings/{id}/payment")
    msg(315, 395, 125, "POST /api/tracking/internal/register")
    msg(315, 225, 95, "PaymentResponse")
    msg(225, 135, 75, "invoiceId + trackingNumber")
    msg(135, 55, 55, "Show success / invoice")

    return d


def build_service_dependency_graph():
    d = Drawing(520, 280)

    def node(x, y, text, color):
        d.add(Rect(x, y, 120, 36, fillColor=color, strokeColor=colors.HexColor("#111827"), strokeWidth=1))
        d.add(String(x + 8, y + 21, text, fontName="Helvetica-Bold", fontSize=8.5, fillColor=colors.white))

    node(20, 210, "auth-service", colors.HexColor("#6D28D9"))
    node(200, 210, "booking-service", colors.HexColor("#059669"))
    node(380, 210, "invoice-service", colors.HexColor("#C2410C"))
    node(110, 130, "payment-service", colors.HexColor("#B45309"))
    node(290, 130, "tracking-service", colors.HexColor("#1D4ED8"))
    node(200, 50, "booking-history-service", colors.HexColor("#7C2D12"))

    def edge(x1, y1, x2, y2, label):
        d.add(Line(x1, y1, x2, y2, strokeColor=colors.HexColor("#111827"), strokeWidth=1))
        d.add(Polygon([x2, y2, x2 - 6, y2 + 3, x2 - 6, y2 - 3], fillColor=colors.HexColor("#111827"), strokeColor=colors.HexColor("#111827")))
        d.add(String((x1 + x2) / 2 - 25, (y1 + y2) / 2 + 4, label, fontName="Helvetica", fontSize=7, fillColor=colors.HexColor("#1F2937")))

    edge(230, 148, 230, 210, "fetch/update")  # payment -> booking
    edge(230, 148, 410, 210, "create invoice") # payment -> invoice
    edge(230, 130, 320, 130, "register shipment") # payment -> tracking
    edge(350, 148, 260, 210, "backfill read") # tracking -> booking
    edge(260, 68, 320, 130, "tracking enrich") # history -> tracking

    return d


def add_footer(canvas, doc):
    canvas.saveState()
    canvas.setFont("Helvetica", 8)
    canvas.setFillColor(colors.HexColor("#6B7280"))
    canvas.drawString(30, 18, "PSM Microservices Workflow Guide")
    canvas.drawRightString(A4[0] - 30, 18, f"Page {doc.page}")
    canvas.restoreState()


def main():
    doc = SimpleDocTemplate(
        OUTPUT_PATH,
        pagesize=A4,
        leftMargin=28,
        rightMargin=28,
        topMargin=28,
        bottomMargin=28,
        title="PSM Project Workflow Guide",
        author="GitHub Copilot",
    )

    story = []

    story.append(heading("PSM Project Workflow Guide", 1))
    story.append(body("A simple, visual explanation of how all microservices in this project communicate and how data flows from screen to database."))
    story.append(Spacer(1, 8))

    summary_table = Table(
        [
            ["Layer", "Main Components", "Purpose"],
            ["UI", "Angular frontend", "Screens for login, booking, payment, invoice, tracking, and history"],
            ["Entry", "API Gateway (8080)", "Single entry point; routes requests to services using Eureka names"],
            ["Discovery", "Service Registry (8761)", "Directory where services register and discover each other"],
            ["Business", "Auth, Booking, Payment, Invoice, Tracking, Booking History", "Domain-specific logic"],
            ["Data", "H2 (file + memory)", "Persist booking/history in shared file DB; others mostly in-memory"],
        ],
        colWidths=[70, 180, 240],
    )
    summary_table.setStyle(
        TableStyle([
            ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#0F766E")),
            ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
            ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),
            ("FONTSIZE", (0, 0), (-1, -1), 9),
            ("GRID", (0, 0), (-1, -1), 0.5, colors.HexColor("#CBD5E1")),
            ("BACKGROUND", (0, 1), (-1, -1), colors.HexColor("#F8FAFC")),
            ("VALIGN", (0, 0), (-1, -1), "TOP"),
            ("LEFTPADDING", (0, 0), (-1, -1), 6),
            ("RIGHTPADDING", (0, 0), (-1, -1), 6),
            ("TOPPADDING", (0, 0), (-1, -1), 4),
            ("BOTTOMPADDING", (0, 0), (-1, -1), 4),
        ])
    )
    story.append(summary_table)
    story.append(Spacer(1, 10))

    story.append(heading("1) High-Level Architecture", 2))
    story.append(body("Every request from the frontend goes to API Gateway first. Gateway uses Eureka service names (like booking-service) and forwards the request to the correct microservice."))
    story.append(Spacer(1, 6))
    story.append(build_architecture_diagram())

    story.append(PageBreak())

    story.append(heading("2) Service Endpoints and Responsibilities", 2))
    endpoint_table = Table(
        [
            ["Service", "Port", "Main API Base", "What It Does"],
            ["auth-service", "8081", "/auth", "Registration, login, profile lookup"],
            ["booking-service", "8082", "/api/bookings", "Create and manage bookings, payment status update"],
            ["payment-service", "8085", "/api/payments", "Bill lookup and payment processing"],
            ["invoice-service", "8087", "/api/invoices", "Create and fetch invoices"],
            ["tracking-service", "8086", "/api/tracking", "Shipment registration, pickup/status updates, customer tracking"],
            ["booking-history-service", "8083", "/api/history", "Customer/officer history with tracking status enrichment"],
            ["api-gateway", "8080", "Path-based routes", "Forwards /auth and /api/* to proper services"],
        ],
        colWidths=[100, 45, 120, 240],
    )
    endpoint_table.setStyle(
        TableStyle([
            ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#1D4E89")),
            ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
            ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),
            ("FONTSIZE", (0, 0), (-1, -1), 8.8),
            ("GRID", (0, 0), (-1, -1), 0.5, colors.HexColor("#CBD5E1")),
            ("BACKGROUND", (0, 1), (-1, -1), colors.HexColor("#F8FAFC")),
            ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ])
    )
    story.append(endpoint_table)
    story.append(Spacer(1, 10))

    story.append(heading("3) Main User Workflows (Simple Steps)", 2))
    story.append(heading("A. Register + Login", 3))
    story.append(bullet("User submits form in Angular."))
    story.append(bullet("Frontend calls gateway: POST /auth/register or /auth/login."))
    story.append(bullet("Gateway forwards to auth-service using Eureka route lb://auth-service."))
    story.append(bullet("Auth service saves/validates user in its H2 DB and returns role + username."))

    story.append(Spacer(1, 6))
    story.append(heading("B. Create Booking", 3))
    story.append(bullet("Frontend calculates an estimated cost and sends booking payload to /api/bookings."))
    story.append(bullet("booking-service stores booking and marks payment status as PENDING."))
    story.append(bullet("booking-service also writes paid booking snapshots into booking_history table when paid."))

    story.append(Spacer(1, 6))
    story.append(heading("C. Pay Bill (Most Important Cross-Service Flow)", 3))
    story.append(bullet("payment-service first fetches booking details from booking-service."))
    story.append(bullet("If amount matches and booking is valid, payment is stored as SUCCESS."))
    story.append(bullet("payment-service marks booking as PAID via booking-service."))
    story.append(bullet("payment-service creates invoice in invoice-service."))
    story.append(bullet("payment-service registers shipment in tracking-service."))
    story.append(bullet("Frontend receives PaymentResponse with invoice and tracking details."))

    story.append(Spacer(1, 6))
    story.append(build_sequence_diagram_payment_flow())

    story.append(PageBreak())

    story.append(heading("4) Inter-Service Communication Map", 2))
    story.append(body("This project uses synchronous REST calls between services. Payment is the orchestrator service that coordinates Booking, Invoice, and Tracking."))
    story.append(Spacer(1, 6))
    story.append(build_service_dependency_graph())

    story.append(Spacer(1, 10))
    story.append(heading("5) Data Storage Model", 2))
    data_table = Table(
        [
            ["Service", "DB Type", "Stored Where", "Important Note"],
            ["booking-service", "H2 file", "~/psm_shared_db.mv.db", "Shared with booking-history-service"],
            ["booking-history-service", "H2 file", "~/psm_shared_db.mv.db", "Reads same bookings/history data"],
            ["auth-service", "H2 memory", "RAM only", "Data resets when service restarts"],
            ["payment-service", "H2 memory", "RAM only", "Payment rows not persisted across restart"],
            ["invoice-service", "H2 memory", "RAM only", "Invoice rows reset on restart"],
            ["tracking-service", "H2 memory", "RAM only", "Tracking rows reset on restart"],
        ],
        colWidths=[110, 70, 140, 185],
    )
    data_table.setStyle(
        TableStyle([
            ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#7C2D12")),
            ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
            ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),
            ("FONTSIZE", (0, 0), (-1, -1), 8.8),
            ("GRID", (0, 0), (-1, -1), 0.5, colors.HexColor("#CBD5E1")),
            ("BACKGROUND", (0, 1), (-1, -1), colors.HexColor("#FFFBEB")),
            ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ])
    )
    story.append(data_table)

    story.append(Spacer(1, 10))
    story.append(heading("6) Why You Sometimes See Startup Conflicts", 2))
    story.append(bullet("If old Java processes are still running, ports like 8083/8086 stay occupied."))
    story.append(bullet("Starting services again creates duplicate instances and causes 'Port already in use' errors."))
    story.append(bullet("Always run stop/cleanup first, then start the VS Code task group."))

    story.append(Spacer(1, 10))
    story.append(heading("7) Simple Mental Model", 2))
    story.append(body(
        "Think of this system like a courier office: the frontend is the reception desk, API Gateway is the dispatcher, Eureka is the staff directory, "
        "booking-service creates the parcel order, payment-service confirms money and triggers invoice + shipment creation, tracking-service tracks parcel movement, "
        "and booking-history-service prepares readable history views for customer and officer dashboards."
    ))

    doc.build(story, onFirstPage=add_footer, onLaterPages=add_footer)


if __name__ == "__main__":
    main()
