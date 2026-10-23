package ui;

import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Labeled;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

/** Small in-app English/Arabic localization helper. */
public final class I18n {
    private static final String ORIGINAL_TEXT = "i18n.originalText";
    private static final String ORIGINAL_PROMPT = "i18n.originalPrompt";
    private static final String LANGUAGE_BUTTON = "language-toggle";
    private static final Preferences PREFERENCES = Preferences.userNodeForPackage(I18n.class);
    private static final List<WeakReference<Stage>> STAGES = new ArrayList<>();
    private static boolean arabic = PREFERENCES.getBoolean("arabic", true);

    private static final Map<String, String> ARABIC = Map.ofEntries(
            Map.entry("Student Management System", "نظام إدارة الطلاب"),
            Map.entry("Student Management", "إدارة الطلاب"),
            Map.entry("Student Portal", "بوابة الطلاب"),
            Map.entry("STUDENT PORTAL", "بوابة الطلاب"),
            Map.entry("Sign in to continue", "سجّل الدخول للمتابعة"),
            Map.entry("SIGN IN", "تسجيل الدخول"), Map.entry("SIGN UP", "إنشاء حساب"),
            Map.entry("Username", "اسم المستخدم"), Map.entry("Password", "كلمة المرور"),
            Map.entry("LOGIN", "دخول"),
            Map.entry("Secure student and payment records", "حافظ على سجلات الطلاب والمدفوعات بأمان"),
            Map.entry("Don't have an account? Sign up", "ليس لديك حساب؟ أنشئ حسابًا"),
            Map.entry("Create your account", "أنشئ حسابك"),
            Map.entry("Set up your student management workspace", "أنشئ مساحة عمل لإدارة الطلاب"),
            Map.entry("Full name", "الاسم الكامل"), Map.entry("Confirm password", "تأكيد كلمة المرور"),
            Map.entry("CREATE ACCOUNT", "إنشاء الحساب"),
            Map.entry("Already have an account? Sign in", "لديك حساب بالفعل؟ سجّل الدخول"),
            Map.entry("Students", "الطلاب"), Map.entry("Payments", "المدفوعات"),
            Map.entry("Remaining", "المتبقي"), Map.entry("Cash", "نقدي"),
            Map.entry("Bank", "بنكي"), Map.entry("Total Income", "إجمالي الدخل"),
            Map.entry("Search students", "البحث عن طالب"),
            Map.entry("Search by name, phone, or birth year...", "ابحث بالاسم أو الهاتف أو سنة الميلاد..."),
            Map.entry("Birth year", "سنة الميلاد"), Map.entry("Birth Year", "سنة الميلاد"),
            Map.entry("All years", "كل السنوات"),
            Map.entry("ID", "الرقم"), Map.entry("Student Name", "اسم الطالب"),
            Map.entry("Phone", "الهاتف"), Map.entry("Registration Date", "تاريخ التسجيل"),
            Map.entry("Notes", "ملاحظات"), Map.entry("Add Student", "إضافة طالب"),
            Map.entry("Payment History", "سجل المدفوعات"), Map.entry("Edit", "تعديل"),
            Map.entry("Delete", "حذف"), Map.entry("Reports", "التقارير"),
            Map.entry("Students Report", "تقرير الطلاب"), Map.entry("Students CSV", "ملف الطلاب CSV"),
            Map.entry("Payments Report", "تقرير المدفوعات"), Map.entry("Payments CSV", "ملف المدفوعات CSV"),
            Map.entry("Refresh", "تحديث"), Map.entry("Logout", "تسجيل الخروج"),
            Map.entry("Student details", "بيانات الطالب"),
            Map.entry("Enter the student's information below.", "أدخل بيانات الطالب أدناه."),
            Map.entry("Weekly attendance days", "أيام الدوام الأسبوعية"),
            Map.entry("Select one or more days; attendance is only recorded on selected days.",
                    "اختر يومًا واحدًا أو أكثر؛ يُسجّل الحضور في الأيام المحددة فقط."),
            Map.entry("Schedule effective from", "يبدأ الجدول من تاريخ"),
            Map.entry("Sunday", "الأحد"), Map.entry("Monday", "الاثنين"), Map.entry("Tuesday", "الثلاثاء"),
            Map.entry("Wednesday", "الأربعاء"), Map.entry("Thursday", "الخميس"),
            Map.entry("Friday", "الجمعة"), Map.entry("Saturday", "السبت"),
            Map.entry("Attendance", "الحضور والغياب"),
            Map.entry("Record attendance", "تسجيل الحضور"),
            Map.entry("Daily attendance", "الحضور اليومي"),
            Map.entry("Choose a date and record the scheduled students.", "اختر التاريخ وسجّل حالة الطلاب المجدولين."),
            Map.entry("Attendance date", "تاريخ الحضور"), Map.entry("Load day", "عرض اليوم"),
            Map.entry("Mark all present", "تسجيل الجميع حاضرين"),
            Map.entry("Save attendance", "حفظ الحضور"), Map.entry("Export period report", "تصدير تقرير فترة"),
            Map.entry("Report from", "التقرير من"), Map.entry("Report to", "التقرير إلى"),
            Map.entry("Scheduled students", "الطلاب المجدولون"), Map.entry("Attendance status", "حالة الحضور"),
            Map.entry("Attendance notes", "ملاحظات الحضور"), Map.entry("Not marked", "غير مسجل"),
            Map.entry("No content in table", "لا توجد بيانات في الجدول"),
            Map.entry("Present", "حاضر"), Map.entry("Absent", "غائب"), Map.entry("Late", "متأخر"),
            Map.entry("Excused absence", "غياب بعذر"), Map.entry("Attendance_Report", "تقرير_الحضور"),
            Map.entry("Name", "الاسم"), Map.entry("Student name", "اسم الطالب"),
            Map.entry("Year of birth", "سنة الميلاد"), Map.entry("Phone number", "رقم الهاتف"),
            Map.entry("e.g. 2008", "مثال: 2008"),
            Map.entry("Registration date", "تاريخ التسجيل"), Map.entry("Optional notes", "ملاحظات اختيارية"),
            Map.entry("Cancel", "إلغاء"), Map.entry("Save student", "حفظ الطالب"),
            Map.entry("Student payment", "دفعة الطالب"),
            Map.entry("Record a payment and review the remaining balance.", "سجّل دفعة وراجع المبلغ المتبقي."),
            Map.entry("Student", "الطالب"), Map.entry("Payment method", "طريقة الدفع"),
            Map.entry("Cash amount", "المبلغ النقدي"), Map.entry("Bank amount", "المبلغ البنكي"),
            Map.entry("Amount due", "المبلغ المستحق"), Map.entry("Paid", "المدفوع"),
            Map.entry("Save payment", "حفظ الدفعة"), Map.entry("Payment history", "سجل المدفوعات"),
            Map.entry("Totals include all payment records; filters apply to the table and CSV export.",
                    "تشمل الإجماليات جميع الدفعات؛ وتُطبّق عوامل التصفية على الجدول وملف CSV."),
            Map.entry("Total paid", "إجمالي المدفوع"),
            Map.entry("Search notes, method, date, or amount...", "ابحث بالملاحظات أو الطريقة أو التاريخ أو المبلغ..."),
            Map.entry("All methods", "كل الطرق"), Map.entry("Mixed", "نقدي + بنكي"),
            Map.entry("From date", "من تاريخ"), Map.entry("To date", "إلى تاريخ"),
            Map.entry("Clear", "مسح"), Map.entry("Date", "التاريخ"),
            Map.entry("Method", "الطريقة"),
            Map.entry("Export CSV", "تصدير CSV"),
            Map.entry("CSV files", "ملفات CSV"), Map.entry("Amount Due", "المبلغ المستحق"),
            Map.entry("Student payment report", "تقرير دفعات الطالب"),
            Map.entry("Close", "إغلاق"), Map.entry("Add payment", "إضافة دفعة"),
            Map.entry("Maximize or restore window", "تكبير أو استعادة النافذة"),
            Map.entry("Minimize window", "تصغير النافذة"), Map.entry("Close window", "إغلاق النافذة"),
            Map.entry("Switch language", "تبديل اللغة"),
            Map.entry("Maximize", "تكبير"), Map.entry("Minimize", "تصغير"),
            Map.entry("Warning", "تنبيه"), Map.entry("Error", "خطأ"), Map.entry("OK", "موافق"),
            Map.entry("Success", "نجاح"), Map.entry("Database Error", "خطأ في قاعدة البيانات"),
            Map.entry("Login Failed", "فشل تسجيل الدخول"), Map.entry("Export failed", "فشل التصدير"),
            Map.entry("Please enter username and password.", "أدخل اسم المستخدم وكلمة المرور."),
            Map.entry("Invalid username or password.", "اسم المستخدم أو كلمة المرور غير صحيح."),
            Map.entry("Could not connect to the database. Check your database configuration.",
                    "تعذر الاتصال بقاعدة البيانات. تحقق من إعدادات الاتصال."),
            Map.entry("Please fill all fields.", "يرجى تعبئة جميع الحقول."),
            Map.entry("Full name must be at least 2 characters.", "يجب ألا يقل الاسم الكامل عن حرفين."),
            Map.entry("Username must be at least 4 characters.", "يجب ألا يقل اسم المستخدم عن 4 أحرف."),
            Map.entry("Username must be 4-50 characters using English letters, numbers, dots, underscores, or hyphens.",
                    "يجب أن يتكون اسم المستخدم من 4 إلى 50 خانة باستخدام أحرف إنجليزية أو أرقام، ويمكن استخدام النقطة والشرطة السفلية والواصلة."),
            Map.entry("Password must be at least 6 characters.", "يجب ألا تقل كلمة المرور عن 6 أحرف."),
            Map.entry("Passwords do not match.", "كلمتا المرور غير متطابقتين."),
            Map.entry("Username already exists.", "اسم المستخدم مستخدم بالفعل."),
            Map.entry("Account created successfully.", "تم إنشاء الحساب بنجاح."),
            Map.entry("Please select a student first.", "يرجى اختيار طالب أولًا."),
            Map.entry("Please select a payment first.", "يرجى اختيار دفعة أولًا."),
            Map.entry("Are you sure you want to delete this student?", "هل أنت متأكد من حذف هذا الطالب؟"),
            Map.entry("Are you sure you want to delete this payment?", "هل أنت متأكد من حذف هذه الدفعة؟"),
            Map.entry("Failed to delete student.", "تعذر حذف الطالب."),
            Map.entry("Payment deleted successfully.", "تم حذف الدفعة بنجاح."),
            Map.entry("The payment record is no longer available.", "سجل الدفعة لم يعد متاحًا."),
            Map.entry("Could not load payment data for export.", "تعذر تحميل بيانات المدفوعات للتصدير."),
            Map.entry("Please fill all required fields.", "يرجى تعبئة جميع الحقول المطلوبة."),
            Map.entry("Select at least one attendance day.", "اختر يوم دوام واحدًا على الأقل."),
            Map.entry("The schedule start date cannot be before the student's registration date.", "لا يمكن أن يبدأ جدول الدوام قبل تاريخ تسجيل الطالب."),
            Map.entry("Could not load the student's attendance schedule.", "تعذر تحميل جدول دوام الطالب."),
            Map.entry("No students are scheduled for this date.", "لا يوجد طلاب مجدولون لهذا التاريخ."),
            Map.entry("No students are scheduled for this date. Add weekly days in the student's details.",
                    "لا يوجد طلاب مجدولون لهذا التاريخ. راجع أيام الدوام وتاريخ بدء جدول الطالب من بياناته."),
            Map.entry("Student record is not available for this account.", "سجل الطالب غير متاح لهذا الحساب."),
            Map.entry("A schedule change must start after the current schedule's effective date.", "يجب أن يبدأ الجدول الجديد بعد تاريخ سريان الجدول الحالي."),
            Map.entry("The new schedule must start after the latest recorded attendance.", "يجب أن يبدأ الجدول الجديد بعد آخر سجل حضور."),
            Map.entry("Attendance saved successfully.", "تم حفظ الحضور بنجاح."),
            Map.entry("Mark at least one student's attendance before saving.", "سجّل حالة طالب واحد على الأقل قبل الحفظ."),
            Map.entry("Choose a valid date range.", "اختر فترة زمنية صحيحة."),
            Map.entry("Export a maximum of one year at a time.", "يمكن تصدير سنة واحدة كحد أقصى في كل مرة."),
            Map.entry("No scheduled attendance days were found in this period.", "لم يتم العثور على أيام دوام مجدولة في هذه الفترة."),
            Map.entry("No saved attendance records were found in this period.", "لا توجد سجلات حضور محفوظة في هذه الفترة."),
            Map.entry("Could not load scheduled students for this date.", "تعذر تحميل الطلاب المجدولين لهذا التاريخ."),
            Map.entry("Could not save attendance.", "تعذر حفظ الحضور."),
            Map.entry("Please enter a valid year.", "يرجى إدخال سنة صحيحة."),
            Map.entry("Year of Birth must be numeric.", "يجب أن تكون سنة الميلاد رقمية."),
            Map.entry("Please select a payment method.", "يرجى اختيار طريقة الدفع."),
            Map.entry("Please enter Amount Due.", "يرجى إدخال المبلغ المستحق."),
            Map.entry("Cash Amount cannot be negative.", "لا يمكن أن يكون المبلغ النقدي سالبًا."),
            Map.entry("Bank Amount cannot be negative.", "لا يمكن أن يكون المبلغ البنكي سالبًا."),
            Map.entry("Amount Due must be greater than zero.", "يجب أن يكون المبلغ المستحق أكبر من صفر."),
            Map.entry("Please enter the Cash Amount.", "يرجى إدخال المبلغ النقدي."),
            Map.entry("Bank Amount must be 0 when payment method is Cash.", "يجب أن يكون المبلغ البنكي صفرًا عند الدفع نقدًا."),
            Map.entry("Please enter the Bank Amount.", "يرجى إدخال المبلغ البنكي."),
            Map.entry("Cash Amount must be 0 when payment method is Bank.", "يجب أن يكون المبلغ النقدي صفرًا عند الدفع عبر البنك."),
            Map.entry("Enter Cash or Bank Amount.", "أدخل مبلغًا نقديًا أو بنكيًا."),
            Map.entry("Mixed payment requires positive cash and bank amounts.",
                    "يتطلب الدفع النقدي والبنكي إدخال مبلغ موجب في كلا الحقلين."),
            Map.entry("Paid amount cannot be greater than Amount Due.", "لا يمكن أن يتجاوز المدفوع المبلغ المستحق."),
            Map.entry("The student or payment record is no longer available.", "الطالب أو سجل الدفعة لم يعد متاحًا."),
            Map.entry("Payment added successfully.", "تمت إضافة الدفعة بنجاح."),
            Map.entry("Payment updated successfully.", "تم تحديث الدفعة بنجاح."),
            Map.entry("Enter valid amounts with no more than 2 decimal places.", "أدخل مبالغ صحيحة بحد أقصى منزلتين عشريتين."),
            Map.entry("Export complete", "اكتمل التصدير"),
            Map.entry("CSV file saved successfully.", "تم حفظ ملف CSV بنجاح."),
            Map.entry("Save PDF Report", "حفظ تقرير PDF"),
            Map.entry("PDF Files", "ملفات PDF"),
            Map.entry("STUDENT MANAGEMENT SYSTEM", "نظام إدارة الطلاب"),
            Map.entry("Student Payment Report", "تقرير دفعات الطالب"),
            Map.entry("Generated Date", "تاريخ إنشاء التقرير"),
            Map.entry("Generated Time", "وقت إنشاء التقرير"),
            Map.entry("Total Students", "عدد الطلاب"), Map.entry("Total Payments", "عدد الدفعات"),
            Map.entry("Payment Summary", "ملخص المدفوعات"), Map.entry("Student Information", "معلومات الطالب"),
            Map.entry("Value", "القيمة"), Map.entry("Total", "الإجمالي"),
            Map.entry("Cash Income", "الدخل النقدي"), Map.entry("Bank Income", "الدخل البنكي"),
            Map.entry("Cash Paid", "المدفوع نقدًا"), Map.entry("Bank Paid", "المدفوع عبر البنك"),
            Map.entry("Total Paid", "إجمالي المدفوع"),
            Map.entry("Enrollment Date", "تاريخ التسجيل"), Map.entry("Payment List", "قائمة الدفعات"),
            Map.entry("Generated by Student Management System", "تم إنشاء التقرير بواسطة نظام إدارة الطلاب"),
            Map.entry("Students report created successfully.", "تم إنشاء تقرير الطلاب بنجاح."),
            Map.entry("Payments report created successfully.", "تم إنشاء تقرير المدفوعات بنجاح."),
            Map.entry("Student payment report created successfully.", "تم إنشاء تقرير دفعات الطالب بنجاح."),
            Map.entry("Failed to create PDF report.", "تعذر إنشاء تقرير PDF."),
            Map.entry("Add Payment", "إضافة دفعة"), Map.entry("Edit Payment", "تعديل الدفعة"),
            Map.entry("Delete Student", "حذف الطالب"), Map.entry("Delete Payment", "حذف الدفعة"),
            Map.entry("Not Found", "غير موجود")
    );

    private I18n() {}

    public static boolean isArabic() { return arabic; }

    public static String text(String english) {
        if (english == null) return null;
        String canonical = toEnglish(english);
        if (!arabic) return canonical;
        String translated = ARABIC.get(canonical);
        if (translated != null) return translated;
        if (canonical.startsWith("Welcome, ") && canonical.endsWith("!")) {
            return "مرحبًا، " + canonical.substring(9, canonical.length() - 1) + "!";
        }
        if (canonical.matches("\\d+ of \\d+ students")) {
            String[] numbers = canonical.split(" of ");
            return numbers[0] + " من " + numbers[1].replace(" students", " طالبًا");
        }
        if (canonical.matches("\\d+ of \\d+ payments")) {
            String[] numbers = canonical.split(" of ");
            return numbers[0] + " من " + numbers[1].replace(" payments", " دفعة");
        }
        if (canonical.matches("\\d+ scheduled students")) {
            return canonical.substring(0, canonical.indexOf(' ')) + " طالبًا مجدولًا";
        }
        return english;
    }

    public static String paymentMethod(String value) {
        if (value == null) return null;
        return switch (value) {
            case "Cash", "نقدي", "نقدًا" -> arabic ? "نقدي" : "Cash";
            case "Bank", "بنكي", "بنك" -> arabic ? "بنكي" : "Bank";
            case "Mixed", "Cash + Bank", "مختلط", "نقدي + بنكي" -> arabic ? "نقدي + بنكي" : "Mixed";
            default -> value;
        };
    }

    public static String canonicalPaymentMethod(String value) {
        if (value == null) return null;
        return switch (value) {
            case "نقدي", "نقدًا" -> "Cash";
            case "بنكي", "بنك" -> "Bank";
            case "نقدي + بنكي", "مختلط", "Cash + Bank" -> "Mixed";
            default -> value;
        };
    }

    public static void localize(Alert alert) {
        alert.setTitle(text(alert.getTitle()));
        alert.setContentText(text(alert.getContentText()));
        for (javafx.scene.control.ButtonType type : alert.getButtonTypes()) {
            Node button = alert.getDialogPane().lookupButton(type);
            if (button instanceof Labeled labeled) labeled.setText(text(type.getText()));
        }
    }

    public static void register(Stage stage) {
        STAGES.removeIf(ref -> ref.get() == null || ref.get() == stage);
        STAGES.add(new WeakReference<>(stage));
    }

    public static void toggle() {
        arabic = !arabic;
        PREFERENCES.putBoolean("arabic", arabic);
        STAGES.removeIf(ref -> ref.get() == null);
        for (WeakReference<Stage> ref : STAGES) apply(ref.get());
    }

    public static void apply(Stage stage) {
        if (stage == null) return;
        Scene scene = stage.getScene();
        if (scene == null) return;
        Node root = scene.getRoot();
        root.setNodeOrientation(arabic ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);
        Node chrome = root.lookup(".window-chrome");
        if (chrome instanceof HBox titleBar) titleBar.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        translateNode(root);
        updateChildren(root);
    }

    public static void setText(Labeled control, String english) {
        control.getProperties().put(ORIGINAL_TEXT, english);
        control.setText(text(english));
    }

    public static void setText(TableColumn<?, ?> column, String english) {
        column.getProperties().put(ORIGINAL_TEXT, english);
        column.setText(text(english));
    }

    private static void updateChildren(Node node) {
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                translateNode(child);
                updateChildren(child);
            }
        }
        if (node instanceof TableView<?> table) {
            table.getColumns().forEach(I18n::translateTableColumn);
        }
    }

    private static void translateTableColumn(TableColumn<?, ?> column) {
        String original = (String) column.getProperties().computeIfAbsent(ORIGINAL_TEXT,
                key -> column.getText() == null ? "" : column.getText());
        column.setText(text(original));
        column.getColumns().forEach(I18n::translateTableColumn);
    }

    private static void translateNode(Node node) {
        if (node instanceof Labeled labeled) {
            String original = (String) labeled.getProperties().computeIfAbsent(ORIGINAL_TEXT,
                    key -> labeled.getText() == null ? "" : labeled.getText());
            labeled.setText(text(original));
            Tooltip tooltip = labeled.getTooltip();
            if (tooltip != null) tooltip.setText(text(tooltip.getText()));
        }
        if (node instanceof TextInputControl input) {
            // Some JavaFX controls (notably DatePicker's internal editor) bind
            // their prompt text. Setting a bound property throws at runtime.
            if (!input.promptTextProperty().isBound()) {
                String original = (String) input.getProperties().computeIfAbsent(ORIGINAL_PROMPT,
                        key -> input.getPromptText() == null ? "" : input.getPromptText());
                input.setPromptText(text(original));
            }
        }
        if (node instanceof DatePicker picker) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(arabic ? "dd/MM/yyyy" : "MM/dd/yyyy");
            LocalDate currentDate = picker.getValue();
            String oldDisplay = picker.getConverter() == null || currentDate == null
                    ? null : picker.getConverter().toString(currentDate);
            String currentEditorText = picker.getEditor().getText();
            picker.setConverter(new StringConverter<>() {
                @Override public String toString(LocalDate date) {
                    return date == null ? "" : formatter.format(date);
                }
                @Override public LocalDate fromString(String value) {
                    if (value == null || value.isBlank()) return null;
                    try { return LocalDate.parse(value.trim(), formatter); }
                    catch (DateTimeParseException exception) { return null; }
                }
            });
            if (currentDate != null && (currentEditorText == null || currentEditorText.isBlank()
                    || currentEditorText.equals(oldDisplay))) {
                picker.getEditor().setText(formatter.format(currentDate));
            }
        }
        if (node instanceof ComboBox<?> combo) {
            @SuppressWarnings("unchecked") ComboBox<Object> items = (ComboBox<Object>) combo;
            Object originalValue = items.getValue() instanceof String value ? toEnglish(value) : items.getValue();
            List<Object> translated = items.getItems().stream()
                    .map(item -> item instanceof String value ? text(toEnglish(value)) : item).toList();
            items.getItems().setAll(translated);
            if (originalValue != null) items.setValue(text(String.valueOf(originalValue)));
        }
        if (node instanceof Control control && control.getTooltip() != null) {
            control.getTooltip().setText(text(toEnglish(control.getTooltip().getText())));
        }
        if (node instanceof javafx.scene.control.MenuButton menuButton) {
            translateMenuItems(menuButton.getItems());
        }
        if (node.getStyleClass().contains(LANGUAGE_BUTTON)
                && node instanceof Labeled labeled) labeled.setText(arabic ? "English" : "العربية");
        if (node.getStyleClass().contains("window-chrome")) {
            node.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        }
    }

    private static void translateMenuItems(List<MenuItem> items) {
        for (MenuItem item : items) {
            String original = (String) item.getProperties().computeIfAbsent(ORIGINAL_TEXT,
                    key -> item.getText() == null ? "" : item.getText());
            item.setText(text(original));
            if (item instanceof Menu menu) translateMenuItems(menu.getItems());
        }
    }

    private static String toEnglish(String value) {
        if (value == null) return null;
        for (Map.Entry<String, String> entry : ARABIC.entrySet()) {
            if (entry.getValue().equals(value)) return entry.getKey();
        }
        return switch (value) {
            case "نقدي", "نقدًا" -> "Cash";
            case "بنكي", "بنك" -> "Bank";
            case "نقدي + بنكي", "مختلط", "Cash + Bank" -> "Mixed";
            default -> value;
        };
    }
}
