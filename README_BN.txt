SWAR STUDIO — Android Native starter project

এই প্রথম build-এ আছে:
- Native Kotlin + Jetpack Compose UI
- Runtime microphone permission
- AudioRecord দিয়ে live mono PCM capture
- YIN-based monophonic fundamental pitch estimation
- Note name, frequency (Hz), cents deviation display

এটি সম্পূর্ণ Riyaz-সমমানের অ্যাপ নয়; এটি বাস্তব অডিও ইনপুটসহ প্রথম কার্যকর ভিত্তি। Polyphonic analysis, gamaka/meend classification, trained vocal models, tanpura synthesis, lesson library, recording/history এবং automated scoring পরবর্তী আলাদা মডিউল হিসেবে যোগ করতে হবে।

ফোন থেকে APK build:
এই ZIP-এ GitHub Actions workflow দেওয়া আছে। কম্পিউটার ছাড়াই ফোনের browser থেকে GitHub-এ repository তৈরি করে ZIP-এর ভেতরের SwarStudio ফোল্ডারের সব ফাইল upload করলে workflow স্বয়ংক্রিয়ভাবে APK build করবে।

Workflow: .github/workflows/android-apk.yml
Build output: app/build/outputs/apk/debug/app-debug.apk

এটি source project; ZIP-এর মধ্যে আগে থেকে APK নেই। GitHub Actions-এ build সম্পন্ন হলে Actions > সর্বশেষ workflow run > Artifacts থেকে APK download করতে হবে।
