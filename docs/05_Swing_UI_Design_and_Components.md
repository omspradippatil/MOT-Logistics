# 🖥️ Document 5: Swing UI Design & Components

This document details the graphical user interface architecture implemented in **Java Swing & AWT**, covering component hierarchy, layout managers, event listeners, and styling techniques.

---

## 1. Swing Component Hierarchy

The application interface is structured hierarchically:

```
JFrame (Top-level Window)
  └── JTabbedPane (Navigation Tabs)
        ├── JPanel (Dashboard Tab)
        │     ├── JLabel (Welcome Header)
        │     └── JPanel (GridLayout 2x3 for Stat Cards)
        │           └── JPanel (Card with Compound Bevel Border)
        │                 ├── JLabel (Icon + Title)
        │                 └── JLabel (Formatted Number)
        │
        ├── JPanel (Entity Management Tabs: Company / Driver / Vehicle)
        │     ├── JPanel (GridBagLayout Input Form at NORTH)
        │     │     ├── JLabel & JTextField / JComboBox
        │     │     └── JPanel (FlowLayout Action Buttons)
        │     └── JScrollPane (CENTER)
        │           └── JTable (DefaultTableModel Data Grid)
        │
        └── JPanel (Invoice / Payment Tabs)
              ├── JPanel (Selection Filters & Summary Labels)
              └── JScrollPane (JTable with Custom TableCellRenderer)
```

---

## 2. Layout Managers Breakdown

Layout managers determine the geometry and auto-resizing of components. The project uses the standard Java layout managers:

### 1. `BorderLayout`
- **Behavior:** Divides a container into five distinct zones: `NORTH`, `SOUTH`, `EAST`, `WEST`, and `CENTER`.
- **Usage:**
  - Used as the root layout for every panel (`CompanyPanel`, `DriverPanel`, `InvoicePanel`, etc.).
  - Forms reside in `NORTH`, scrollable tables occupy `CENTER` (which expands to fill available space), and summary bars/buttons occupy `SOUTH`.

### 2. `GridBagLayout`
- **Behavior:** The most flexible grid layout in AWT. Allows components to span multiple rows/columns, control cell padding (`insets`), and set anchor alignment.
- **Usage:**
  - Form panels for adding and updating entities.
  - Ensures labels and text fields remain aligned cleanly without overlapping.

### 3. `GridLayout`
- **Behavior:** Divides space into a fixed grid of equal-sized rectangular cells.
- **Usage:**
  - Dashboard stat cards: `new GridLayout(2, 3, 15, 15)` arranges 6 stat cards into 2 rows and 3 columns with 15px horizontal and vertical gaps.

### 4. `FlowLayout`
- **Behavior:** Lines up components left-to-right or center, wrapping onto the next line if the container is resized.
- **Usage:**
  - Action button toolbars: `new FlowLayout(FlowLayout.LEFT, 8, 0)` for Add, Update, Delete, and Clear buttons.

---

## 3. Advanced JTable Handling

### 1. Non-Editable Grid Cells
By default, `JTable` cells can be double-clicked and edited in place, which would create inconsistencies with the database. We disable in-cell editing via an anonymous subclass of `DefaultTableModel`:

```java
tableModel = new DefaultTableModel(columns, 0) {
    @Override
    public boolean isCellEditable(int row, int col) {
        return false; // Grid is strictly read-only; editing happens via the form
    }
};
```

### 2. Row Selection Listener
Clicking any row in the table automatically synchronizes and fills the corresponding text fields in the form:

```java
table.getSelectionModel().addListSelectionListener(e -> {
    if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
        fillFormFromRow();
    }
});
```

### 3. Custom Color Renderer in `PaymentPanel`
Rows in the payment ledger are dynamically color-coded based on their settlement status (`Paid`, `Partially Paid`, or `Pending`):

```java
table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
    @Override
    public Component getTableCellRendererComponent(
            JTable t, Object val, boolean sel, boolean focus, int row, int col) {
        super.getTableCellRendererComponent(t, val, sel, focus, row, col);
        if (!sel) {
            String status = (String) tableModel.getValueAt(row, 7);
            if ("Paid".equals(status)) {
                setBackground(new Color(220, 255, 220)); // Soft Green
            } else if ("Partially Paid".equals(status)) {
                setBackground(new Color(255, 255, 200)); // Soft Yellow
            } else {
                setBackground(new Color(255, 230, 230)); // Soft Red (Pending)
            }
        }
        return this;
    }
});
```

---

## 4. UI Color Palette

The interface follows a clean, professional corporate theme:

| Element | Color Hex / RGB | Purpose |
|---|---|---|
| **Header Banner** | `#004682` / `rgb(0, 70, 130)` | Deep Navy Blue (Transport primary brand) |
| **Driver Header** | `#006432` / `rgb(0, 100, 50)` | Deep Forest Green (Driver view indicator) |
| **Add / Submit** | `#008246` / `rgb(0, 130, 70)` | Emerald Green action buttons |
| **Update / Search** | `#0064B4` / `rgb(0, 100, 180)` | Royal Blue primary action buttons |
| **Delete / Logout** | `#C83232` / `rgb(200, 50, 50)` | Crimson Red cautionary action buttons |
| **Table Headers** | `#004682` / `rgb(0, 70, 130)` | White bold text on Navy background |
