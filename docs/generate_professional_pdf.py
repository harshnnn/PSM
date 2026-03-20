import os
from reportlab.lib.pagesizes import letter
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_LEFT

def generate_pdf():
    doc = SimpleDocTemplate(
        "C:/Users/HARSH/Desktop/SpringBoot/PSM/docs/PSM_Enterprise_Workflow_Guide.pdf",
        pagesize=letter,
        rightMargin=50, leftMargin=50, topMargin=50, bottomMargin=50
    )
    styles = getSampleStyleSheet()
    
    # Custom Styles
    title_style = ParagraphStyle(name='CustomTitle', parent=styles['Heading1'], fontSize=24, spaceAfter=20, alignment=TA_CENTER, textColor=colors.HexColor('#1f497d'))
    subtitle_style = ParagraphStyle(name='CustomSub', parent=styles['Heading2'], fontSize=14, spaceAfter=40, alignment=TA_CENTER, textColor=colors.gray)
    h1_style = ParagraphStyle(name='Heading1', parent=styles['Heading1'], fontSize=18, spaceAfter=15, textColor=colors.HexColor('#2c4d82'))
    h2_style = ParagraphStyle(name='Heading2', parent=styles['Heading2'], fontSize=14, spaceAfter=10, textColor=colors.HexColor('#4f81bd'))
    normal_style = ParagraphStyle(name='Normal', parent=styles['Normal'], fontSize=10, spaceAfter=8, leading=14)
    step_num_style = ParagraphStyle(name='StepNum', parent=styles['Normal'], fontSize=12, textColor=colors.white, alignment=TA_CENTER, fontName='Helvetica-Bold')

    elements = []

    # --- COVER PAGE ---
    elements.append(Spacer(1, 100))
    elements.append(Paragraph("System Architecture & Workflow Document", title_style))
    elements.append(Paragraph("Postal Service Management (PSM) Microservices", subtitle_style))
    elements.append(Spacer(1, 150))
    elements.append(Paragraph("Generated automatically to provide a clean, professional, and easy-to-understand overview of system architecture, customer journeys, and officer operations.", normal_style))
    elements.append(PageBreak())

    # --- SYSTEM ARCHITECTURE (LAYERED MODEL) ---
    elements.append(Paragraph("1. System Architecture Diagram", h1_style))
    elements.append(Paragraph("The system is designed sequentially across five distinct layers. To avoid convoluted visual graphs, this architecture is modeled as a strictly layered application stack. Network traffic flows vertically downwards safely without crisscrossing lines.", normal_style))
    elements.append(Spacer(1, 15))

    # Architecture built gracefully using perfectly aligned tables
    arch_data = [
        [Paragraph("<b>[ Layer 1: Actors ]</b>", ParagraphStyle('C', alignment=TA_CENTER))],
        ["Customers / Users", "Officers / Admins"],
    ]
    t_actors = Table(arch_data, colWidths=[250, 250])
    t_actors.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), colors.HexColor('#e8f1fa')),
        ('ALIGN', (0,0), (-1,-1), 'CENTER'),
        ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
        ('GRID', (0,0), (-1,-1), 1, colors.white),
        ('SPAN', (0,0), (1,0)),
        ('BOTTOMPADDING', (0, 0), (-1,-1), 10),
        ('TOPPADDING', (0, 0), (-1,-1), 10),
        ('FONTNAME', (0,0), (-1,-1), 'Helvetica-Bold')
    ]))

    t_front = Table([["[ Layer 2: Client Web UI ]\nAngular Frontend Application (localhost:4200)"]], colWidths=[500])
    t_front.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), colors.HexColor('#d9e1f2')),
        ('ALIGN', (0,0), (-1,-1), 'CENTER'),
        ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
        ('BOX', (0,0), (-1,-1), 1, colors.HexColor('#4f81bd')),
        ('BOTTOMPADDING', (0, 0), (-1,-1), 15),
        ('TOPPADDING', (0, 0), (-1,-1), 15),
        ('FONTNAME', (0,0), (-1,-1), 'Helvetica-Bold')
    ]))

    t_gate = Table([["[ Layer 3: Infrastructure Routes ]\nSpring Cloud API Gateway (8080)   &   Service Registry (Eureka 8761)"]], colWidths=[500])
    t_gate.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), colors.HexColor('#b4c6e7')),
        ('ALIGN', (0,0), (-1,-1), 'CENTER'),
        ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
        ('BOX', (0,0), (-1,-1), 1, colors.HexColor('#4f81bd')),
        ('BOTTOMPADDING', (0, 0), (-1,-1), 15),
        ('TOPPADDING', (0, 0), (-1,-1), 15),
        ('FONTNAME', (0,0), (-1,-1), 'Helvetica-Bold')
    ]))

    services_data = [
        ["[ Layer 4: Microservices ]", "", ""],
        ["Auth (8081)", "Booking (8082)", "History (8083)"],
        ["Payment (8085)", "Tracking (8086)", "Invoice (8087)"]
    ]
    t_serv = Table(services_data, colWidths=[166, 167, 167])
    t_serv.setStyle(TableStyle([
        ('SPAN', (0,0), (2,0)),
        ('BACKGROUND', (0,0), (-1,-1), colors.HexColor('#8eaadb')),
        ('TEXTCOLOR', (0,0), (-1,-1), colors.white),
        ('ALIGN', (0,0), (-1,-1), 'CENTER'),
        ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
        ('GRID', (0,0), (-1,-1), 2, colors.white),
        ('BOTTOMPADDING', (0, 0), (-1,-1), 12),
        ('TOPPADDING', (0, 0), (-1,-1), 12),
        ('FONTNAME', (0,0), (-1,-1), 'Helvetica-Bold')
    ]))

    db_data = [
        ["[ Layer 5: Data Persistence ]", ""],
        ["Shared File DB (psm_shared_db)\nHolds: Bookings, History", "In-Memory DBs per service\nHolds: Auth/Payment/Invoice"]
    ]
    t_db = Table(db_data, colWidths=[250, 250])
    t_db.setStyle(TableStyle([
        ('SPAN', (0,0), (1,0)),
        ('BACKGROUND', (0,0), (-1,-1), colors.HexColor('#cad1df')),
        ('ALIGN', (0,0), (-1,-1), 'CENTER'),
        ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
        ('GRID', (0,0), (-1,-1), 1, colors.white),
        ('BOTTOMPADDING', (0, 0), (-1,-1), 10),
        ('TOPPADDING', (0, 0), (-1,-1), 10),
        ('FONTNAME', (0,0), (-1,-1), 'Helvetica-Bold')
    ]))

    down_arrow = Paragraph("<font size='16' color='#4f81bd'>&#8595;</font>", ParagraphStyle('C', alignment=TA_CENTER))

    elements.append(t_actors)
    elements.append(Spacer(1, 5))
    elements.append(down_arrow)
    elements.append(Spacer(1, 5))
    elements.append(t_front)
    elements.append(Spacer(1, 5))
    elements.append(down_arrow)
    elements.append(Spacer(1, 5))
    elements.append(t_gate)
    elements.append(Spacer(1, 5))
    elements.append(down_arrow)
    elements.append(Spacer(1, 5))
    elements.append(t_serv)
    elements.append(Spacer(1, 5))
    elements.append(down_arrow)
    elements.append(Spacer(1, 5))
    elements.append(t_db)
    
    elements.append(PageBreak())

    # --- CUSTOMER FLOW ---
    elements.append(Paragraph("2. New User (Customer) Workflow", h1_style))
    elements.append(Paragraph("When a new customer uses the system, they proceed through an automated funnel from sign-up to package tracking.", normal_style))
    elements.append(Spacer(1, 15))

    user_steps = [
        ["1", "Registration & Auth", "User registers via Frontend. The Gateway routes it to the Auth Service. Data is kept in an in-memory DB. Login distributes an active session role."],
        ["2", "Create Booking", "User fills in sender/receiver details. Handled by Booking Service. The result saves to the shared H2 Database."],
        ["3", "Checkout / Payment", "User pays for the shipping. Payment Service orchestrates the transaction. Upon success, Payment calls both the Invoice Service and Tracking Service over standard HTTP via RestTemplate."],
        ["4", "Invoice Generation", "Triggered automatically by the Payment workflow, the Invoice Service issues a receipt and marks the order paid."],
        ["5", "Tracking Initialized", "Also triggered automatically, the Tracking Service provisions a new shipment ID linked directly to the booking ID."],
        ["6", "View History", "User visits their dashboard. The frontend queries the History Service, which combines Database Booking records with RestTemplate Track status mappings."]
    ]

    for step in user_steps:
        num_cell = Table([[Paragraph(step[0], step_num_style)]], colWidths=[30], rowHeights=[30])
        num_cell.setStyle(TableStyle([
            ('BACKGROUND', (0,0), (-1,-1), colors.HexColor('#4f81bd')),
            ('ALIGN', (0,0), (-1,-1), 'CENTER'),
            ('VALIGN', (0,0), (-1,-1), 'MIDDLE')
        ]))
        
        detail_cell = Paragraph(f"<b><font size='12'>{step[1]}</font></b><br/><br/>{step[2]}", normal_style)
        
        t_step = Table([[num_cell, detail_cell]], colWidths=[45, 455])
        t_step.setStyle(TableStyle([
            ('VALIGN', (0,0), (-1,-1), 'TOP'),
            ('BOTTOMPADDING', (0,0), (-1,-1), 15)
        ]))
        elements.append(t_step)

    elements.append(PageBreak())

    # --- OFFICER FLOW ---
    elements.append(Paragraph("3. Officer Operations Workflow", h1_style))
    elements.append(Paragraph("Officers are internal users tasked with fulfilling shipments, scheduling physical pickups, and updating the logistic status in real-time.", normal_style))
    elements.append(Spacer(1, 15))

    officer_steps = [
        ["A", "Officer Login", "Officer authenticates through the angular frontend using secure internal credentials (e.g. officer01). The system UI detects the 'OFFICER' role and unlocks hidden application tabs."],
        ["B", "View Active Shipments", "Officer heads to Logistics Hub sections (`/pickup-scheduling` and `/delivery-status`). The UI queries the tracking service endpoints via the Gateway retrieving all systemic active user shipments."],
        ["C", "Schedule Pickup", "The Officer selects an incoming request and updates a shipment's state. The logic validates strict role permissions before advancing bounds."],
        ["D", "Mark In-Transit", "The shipment physically progresses. The Officer uses the simple UI interface to post status checkpoints iteratively (e.g. `IN_TRANSIT`)."],
        ["E", "Delivery Confirmation", "As completion takes place, the Officer marks `DELIVERED`. The Tracking DB records this. Customers will simultaneously observe this on their synced dashboard via History services."]
    ]

    for step in officer_steps:
        num_cell = Table([[Paragraph(step[0], step_num_style)]], colWidths=[30], rowHeights=[30])
        num_cell.setStyle(TableStyle([
            ('BACKGROUND', (0,0), (-1,-1), colors.HexColor('#e36c09')), # Orange
            ('ALIGN', (0,0), (-1,-1), 'CENTER'),
            ('VALIGN', (0,0), (-1,-1), 'MIDDLE')
        ]))
        
        detail_cell = Paragraph(f"<b><font size='12'>{step[1]}</font></b><br/><br/>{step[2]}", normal_style)
        
        t_step = Table([[num_cell, detail_cell]], colWidths=[45, 455])
        t_step.setStyle(TableStyle([
            ('VALIGN', (0,0), (-1,-1), 'TOP'),
            ('BOTTOMPADDING', (0,0), (-1,-1), 15)
        ]))
        elements.append(t_step)

    doc.build(elements)
    print("PDF successfully generated.")

if __name__ == "__main__":
    generate_pdf()
