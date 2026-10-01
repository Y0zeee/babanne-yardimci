# Babaanne Telefonu — Aile Kurulum Rehberi

İnternetsiz, tamamen sesli Türkçe asistan. Bu rehber telefonu kuran aile üyesi içindir.
Kurulum ekranına: ana ekran → sol üst menü → **Ayarlar** → en alttaki **Kurulum**.
(Babaanne yanlışlıkla açmasın diye ana ekranda yoktur.)

## 1. İzinler
Kurulum ekranında 7 izin satırı vardır (mikrofon, telefon/arama, rehber, konum, SMS,
arama kaydı, bildirim). Kırmızı olanların yanındaki **İzin ver** düğmesine basın;
hepsi yeşil olmalıdır.

## 2. Güç düğmesi kısayolu
Ayarlar → Ek ayarlar → Düğme kısayolları → **"Google Asistanı başlat"** → **"Güç düğmesini 0,5 sn basılı tut"**.
Varsayılan asistan bu uygulama olduğu için (3. adım) kısayol bunu açar: babaanne güç düğmesini
basılı tutar → kısa titreşim + "Buyur" → konuşur. (Kısayol MIUI'de yalnız tam ekran hareket
navigasyonunda görünür; görünmezse Ayarlar → Ek ayarlar → Tam ekran → "Hareketler".)

## 3. Varsayılan asistan
Kurulum ekranında **Varsayılan asistan** satırındaki **Ayarları aç** düğmesine basın,
"Varsayılan dijital asistan uygulaması" olarak bu uygulamayı seçin.

## 4. MIUI arka plan ayarları
- **Otomatik başlatma ayarı**: uygulamayı listede açın.
- **Pil: Kısıtlama yok**: pil tasarrufunu "Kısıtlama yok" yapın.
Düğme açılmazsa uygulama ayrıntıları sayfası açılır; oradan elle ayarlayın.

## 5. Konuşma tanıma modeli
Wi-Fi varken **Modeli indir** düğmesine basın ve "Türkçe model hazır" yazısını bekleyin.
Bu indirme tek seferliktir; sonrasında internet gerekmez.

## 6. Türkçe çevrimdışı ses (TTS)
Ayarlar → Ek ayarlar → Erişilebilirlik → Metinden konuşmaya → Tercih edilen motor
(Google veya Samsung/başka bir motor) → Dil: Türkçe → ses verisini indirin
(çevrimdışı kullanım için). Sonra Kurulum ekranındaki **Dene: saat kaç** düğmesiyle deneyin.

## 7. yardımcı.json
Dosya adı: `yardımcı.json`. Yeri: uygulamanın harici dosya klasörü
(`/sdcard/Android/data/org.stypox.dicio.main/files/yardımcı.json`).
Örnek (yalnız örnek numaralardır, gerçek numara yazın ama bu depoya asla koymayın):

```json
{
  "kisiler": [
    { "adlar": ["kızım", "büyük kızım"], "numara": "+90 555 000 00 01", "acil_sira": 1 },
    { "adlar": ["oğlum"], "numara": "+90 555 000 00 02", "acil_sira": 2 },
    { "adlar": ["komşu"], "numara": "+90 555 000 00 03" }
  ]
}
```

Kurulum ekranındaki **Kişiler** bölümü takma adı, acil sırayı ve numaranın yalnız son
2 hanesini gösterir.

## 8. Kur'an ve türkü dosyaları
Dosyaları `.mp3` olarak şu klasörlere kopyalayın:
- `/sdcard/Yardimci/kuran/` — örn. `yasin.mp3`, `fatiha.mp3`, `mulk.mp3`, `rahman.mp3`, `amme.mp3`
- `/sdcard/Yardimci/turku/` — istediğiniz adlarla `.mp3`

Kurulum ekranındaki **Dosyalar** satırı kaç dosya bulunduğunu gösterir.

## Sık sorunlar
- **Asistan dinlemiyor**: mikrofon izni ve "Türkçe model hazır" durumunu kontrol edin.
- **Ekran kapanınca asistan duruyor**: MIUI otomatik başlatma ve pil "Kısıtlama yok" ayarlarını yapın.
- **Ses çıkmıyor**: Türkçe TTS ses verisi inmemiş olabilir (6. adım) veya medya sesi kısıktır.
- **Arama yapmıyor**: telefon ve rehber izinleri ile `yardımcı.json` takma adlarını kontrol edin.
- **Kur'an/türkü çalmıyor**: klasör ve dosya adlarını (küçük harf, `.mp3`) kontrol edin.
