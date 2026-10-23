# Student Management System

## إعداد اتصال قاعدة البيانات

يحتاج التطبيق إلى قاعدة MySQL باسم `student_management`، وإلى متغيرات البيئة التالية عند تشغيله:

- `DB_USER`: اسم مستخدم MySQL.
- `DB_PASSWORD`: كلمة مرور مستخدم MySQL (يمكن أن تكون فارغة إن كان إعداد MySQL المحلي لديك يستخدم كلمة مرور فارغة).
- `DB_URL`: اختياري؛ افتراضيًا `jdbc:mysql://localhost:3306/student_management`.

في PowerShell، عيّن المتغيرات لجلسة التشغيل الحالية قبل فتح IntelliJ:

```powershell
$env:DB_USER = "root"
$env:DB_PASSWORD = ""
```

أو أضفها إلى **Run Configuration > Environment variables** في IntelliJ. لا تحفظ كلمات المرور في المستودع.

## كلمات المرور

تُخزّن كلمات مرور الحسابات الجديدة باستخدام PBKDF2-HMAC-SHA256 مع ملح عشوائي. عند نجاح تسجيل الدخول لحساب قديم محفوظ بكلمة مرور نصية، يحوّل التطبيق كلمة مروره إلى الصيغة المجزأة تلقائيًا.

