# تكامل قاعدة البيانات - Child Monitor Android App

## معلومات الربط

تم تحديث التطبيق للاتصال بخادم Child Monitor الجديد مع قاعدة البيانات.

### رابط الخادم
```
https://3000-iv9kwo40euydbcxtan4zz-1826f61a.sg1.manus.computer/api/trpc
```

### ملف الاتصال
تم تحديث الملف التالي:
- `app/src/main/java/com/example/childmonitor/api/ApiClient.kt`

## كيفية الاستخدام

### 1. تسجيل دخول الآباء
```kotlin
apiClient.loginParent(
    email = "parent@example.com",
    password = "password123",
    onSuccess = { response ->
        // تم تسجيل الدخول بنجاح
        Log.d("API", "Login successful: $response")
    },
    onError = {
        // فشل تسجيل الدخول
        Log.e("API", "Login failed")
    }
)
```

### 2. إضافة طفل جديد
```kotlin
apiClient.addChild(
    parentId = "1",
    childName = "محمد",
    childPassword = "1234",
    onSuccess = { response ->
        Log.d("API", "Child added: $response")
    },
    onError = {
        Log.e("API", "Failed to add child")
    }
)
```

### 3. تسجيل دخول الطفل
```kotlin
apiClient.loginChild(
    password = "1234",
    onSuccess = { response ->
        Log.d("API", "Child login successful: $response")
    },
    onError = {
        Log.e("API", "Child login failed")
    }
)
```

### 4. إرسال موقع الطفل
```kotlin
apiClient.sendLocation(
    childId = "5",
    latitude = 24.7136,
    longitude = 46.6753,
    address = "الرياض، السعودية"
)
```

### 5. إرسال بيانات استخدام التطبيقات
```kotlin
apiClient.sendAppUsage(
    childId = "5",
    appName = "YouTube",
    packageName = "com.google.android.youtube",
    usageTime = 3600 // بالثواني
)
```

## البيانات المحفوظة في قاعدة البيانات

### جدول الآباء (parents)
- `id`: معرف فريد
- `email`: البريد الإلكتروني
- `passwordHash`: كلمة المرور المشفرة
- `name`: الاسم الكامل
- `createdAt`: تاريخ الإنشاء
- `updatedAt`: تاريخ آخر تحديث

### جدول الأطفال (children)
- `id`: معرف فريد
- `parentId`: معرف الآباء
- `name`: اسم الطفل
- `passwordHash`: كلمة المرور المشفرة
- `deviceId`: معرف الجهاز (اختياري)
- `createdAt`: تاريخ الإنشاء
- `updatedAt`: تاريخ آخر تحديث

### جدول المواقع (locations)
- `id`: معرف فريد
- `childId`: معرف الطفل
- `latitude`: خط العرض
- `longitude`: خط الطول
- `address`: العنوان
- `timestamp`: وقت التسجيل

### جدول استخدام التطبيقات (appUsage)
- `id`: معرف فريد
- `childId`: معرف الطفل
- `appName`: اسم التطبيق
- `packageName`: اسم الحزمة
- `usageTime`: وقت الاستخدام (بالثواني)
- `timestamp`: وقت التسجيل

## ملاحظات مهمة

1. **الأمان**: جميع كلمات المرور يتم تشفيرها قبل الحفظ في قاعدة البيانات
2. **الاتصال**: تأكد من أن الجهاز متصل بالإنترنت
3. **المعرفات**: استخدم معرفات صحيحة للآباء والأطفال
4. **الموقع**: أرسل موقع الطفل بشكل دوري (كل 5-10 دقائق)
5. **الاستخدام**: أرسل بيانات الاستخدام بشكل دوري (كل ساعة أو يومياً)

## استكشاف الأخطاء

### الخطأ: "Invalid email or password"
- تأكد من صحة البريد الإلكتروني وكلمة المرور
- تأكد من أن الحساب مسجل في قاعدة البيانات

### الخطأ: "Child not found"
- تأكد من أن معرف الطفل صحيح
- تأكد من أن الطفل مسجل تحت حسابك

### الخطأ: "Connection timeout"
- تأكد من الاتصال بالإنترنت
- تأكد من أن الخادم يعمل

## تحديث الرابط في المستقبل

إذا تغير رابط الخادم، قم بتحديث السطر التالي في `ApiClient.kt`:

```kotlin
private val baseUrl = "https://new-server-url.com/api/trpc"
```

---

**آخر تحديث**: 2026-03-03
**الإصدار**: 1.0.0
