# Tank Shooter — Android Studio

Bu klasoru Android Studio > Open ile acin. FTC SDK 12.0 temiz resmi arsivinden olusturuldu.
Kaynak: https://github.com/FIRST-Tech-Challenge/FtcRobotController/releases/tag/v12.0

## Kod
TeamCode/src/main/java/org/firstinspires/ftc/teamcode/
- TankShooterTeleOp.java: donanim, joystick ve telemetry.
- RobotSettings.java: RPM, PIDF, yonler, servo konumlari ve zamanlar.
- ShotController.java: bloklamayan sut sirasi.

## Robot Configuration isimleri ve portlar
Kullanicinin bildirdigi fiziksel baglantilar:

| Tur | Port | Robot parcasi | Configuration ismi |
| --- | --- | --- | --- |
| Motor | 0 | Sag drive base | sag_drive |
| Motor | 1 | Sol drive base | sol_drive |
| Motor | 2 | Intake | intake |
| Motor | 3 | Shooter (REV HD Hex) | shooter |
| Servo | 0 | Top tutucu kapak | kapak |

Driver Station > Configure Robot ekraninda ilgili Hub'i secin.
Motors bolumunde yukaridaki motor portlarini ve isimlerini ayarlayin.
Servos bolumunde port 0'i standart pozisyon servosu olarak kapak ismiyle ekleyin.
Konfigurasyonu kaydedip etkinlestirin. Shooter encoder kablosu da motor 3'e
karsilik gelen encoder portuna bagli olmali. Motor tiplerini gercek modellere gore secin.
Kod port numarasini degil bu isimleri kullanir; Robot Configuration henuz cihazda ayarlanmadi.

## Kontroller
- Sol stick Y: ileri/geri; sag stick X: donus. Surus sut sirasinda da devam eder.
- Right bumper: intake ac/kapa. Acikken INTAKE_RPM hedeflenir (500 teker RPM).
- A: intake durur, shooter 3300 RPM hedefler.
- RPM hedefin +/-100 RPM araligina ilk girdiginde 0.5 saniyelik bekleme baslar.
  Bu surede RPM dusse bile sayac sifirlanmaz; sure dolunca kapak ac komutu verilir.
- Kapak hareketi icin 0.20 saniye sonra intake SHOT_INTAKE_RPM hedefiyle baslar (500 teker RPM).
- Ac komutundan 2 saniye sonra kapak kapanir, intake durur ve shooter 2000 RPM'e doner.
- Besleme sirasinda RPM dususu intake'i DURDURMAZ.
- Sut sirasinda A ve RB yok sayilir; yeni komut icin birakip tekrar basin.
- 5 saniyede RPM hazir olmazsa sut iptal edilir; encoder/yon/PIDF kontrol edin.
- X: sutu iptal, kapagi kapat, intake durdur; shooter 2000 RPM'e doner.
- STOP: tum motorlari durdurur, kapagi kapatir. INIT sirasinda shooter donmez.

## Shooter kS / kV / kP / kI / kD
RobotSettings.java icindeki degerler fotografla verilen degerlerdir:
kS=2.5267, kV=0.002407, kP=0.025949, kI=0.0, kD=0.0.
Kaynak tuner kodu bulunamadi. Volt/RPM birimleri degerlerin buyuklugunden
VARSAYILDI; orijinal tuner birimleriyle ve robotta dogrulanmalidir.

volts = kS + kV * hedefRPM + kP * hata + kI * integral(hata dt)
        - kD * olculenRPM_turevi
power = clamp(volts / olculenBataryaVoltaji, 0, 1)

kS: V; kV/kP: V/RPM; kI: V/(RPM*s); kD: V*s/RPM.
3300 RPM'de sifir hata icin 10.4698 V, 2000 RPM'de 7.3407 V istenir.
Motor RUN_WITHOUT_ENCODER modunda; encoder okunur, Hub PIDF kullanilmaz.
Integral doygunluk korumasi vardir; hedef degisince integral sifirlanir.
Gecersiz voltaj olcumunde shooter durur ve telemetry uyari verir.
Katsayilar, batarya voltaji, hedef/olculen RPM ve guc telemetry'de gorunur.
Degerleri degistirdikten sonra Android Studio Run ile yeniden yukleyin.
Canli dashboard yoktur. Bu katsayilar robotta test edilmedi.

## RPM ve servo kalibrasyonu
REV HD Hex reduksiyonsuz 1:1 varsayimi: 28 tick/tur.
3300 RPM = 1540 tick/s; 2000 RPM = 933.333 tick/s.
Motorun icinde reduktor varsa MOTOR_REVS_PER_WHEEL_REV toplam orani icermelidir.
Encoder: https://docs.revrobotics.com/duo-control/sensors/encoders/motor-based-encoders
Servo modeli belirtilmedi: SERVO_TOTAL_TRAVEL_DEG = 180 varsayimdir.
Normal/kapali 90 derece (0.50), sut sirasinda acik 0 derece (0.00).
Gercek acilar servo modeli ve PWM araligina baglidir.
Servo toplam hareketini ve iki konumu fiziksel olarak dogrulayin.
2 saniye gercek servo konumundan degil ac komutundan olculur.
Montaja gore LEFT/RIGHT/INTAKE/SHOOTER/GATE_REVERSED alanlarini ayarlayin.

## Yukleme
1. Android Studio'da bu kok klasoru acin; Gradle Sync'i tamamlayin.
2. Control Hub'i USB ile baglayin ve cihaz olarak secin.
3. Run konfigurasyonunda TeamCode secip Run yapin.
4. Driver Station ile Robot Controller surumlerini uyumlu tutun (12.0).
5. Dogru Robot Configuration'i etkinlestirin; TeleOp > NFR Products FTC-Biobuzz StarterBot secin.
6. Once yon, encoder RPM ve servo konumlarini kontrollu olarak test edin.

Derleme/yazilim testleri fiziksel robot testi degildir. Robot uzerinde test yapilmadi.

## Intake RPM ve PIDF
Intake encoder kablosunu, intake guc kablosunun takili oldugu Hub'da encoder port 2'ye takin.
Motor milindeki 28 tick/tur, REV-41-1603 kartusun 68:13 oraniyla tekerde yaklasik
146.46 tick/tur eder. Tekerin gearbox cikisina dogrudan bagli oldugu varsayilir.
Arada ek aktarim varsa INTAKE_MOTOR_REVS_PER_WHEEL_REV alanina toplam orani yazin.
INTAKE_RPM normal hedef, SHOT_INTAKE_RPM sut besleme hedefidir. Ikisi de teker RPM'idir.
INTAKE_KS/KV/KP/KI/KD alanlari intake hiz kontrolune aittir. Baslangic degerleri
robotta olculmemistir. Driver Station'da olculen ve hedef teker RPM'i gorunur.

## START kontrolu
Surus encoder kullanmaz. Intake ve shooter, hiz geri bildirimi icin encoder okur.
INIT boyunca tum motorlar sifir gucle tutulur. START sonrasi surus, joystickler
0.25 saniye merkezde kaldiktan sonra etkinlesir; merkezde surus gucu sifirdir.
START sonrasi sag teker kendi kendine donuyorsa aktif Robot Configuration'i kontrol edin:
motor 0=sag_drive, 1=sol_drive, 2=intake, 3=shooter. Shooter START ile otomatik doner;
shooter ismi sag motor portuna atanirsa sag teker shooter komutunu alir.
Telemetry sol/sag guc, joystick degerleri ve motor baglanti bilgilerini gosterir.
Bu degisiklik fiziksel arizanin nedenini dogrulamaz; robot testi yapilmadi.

## Batarya voltaji telafisi
Shooter ve intake kontrolculeri voltaj isteklerini her dongude olculen batarya voltajina bolerek motor gucunu hesaplar. PIDF katsayilarini RobotSettings.java dosyasindan ayarlayin. Driver Station'daki Shooter V satiri istenen ve tahmini uygulanan voltaji gosterir. Batarya istenen voltaji karsilayamazsa cikis tam gucle sinirlanir.
