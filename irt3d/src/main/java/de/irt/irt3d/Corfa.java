package de.irt.irt3d;

import de.irt.fortran.Units;
import de.irt.fortran.F77;
import de.irt.fortran.IArr;
import de.irt.irt3d.common.CCHG;
import de.irt.irt3d.common.PBP;
import de.irt.irt3d.common.PROF;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 26.08.17
 * Time: 16:42
 */
public class Corfa extends F77 {

    public Corfa(Units commons) {
        super(commons);
    }

//    Korrekturfaktor im Stadtgebiet
//    Beim Modus ICW=-2 (Clutter als Höhe) ist im Stadtgebiet
//    die Dämpfung aus der Morpho-Korrektur und der Beugung
//    an Gebäuden zu groß, zumindest für kleine Sendeantennen-
//    höhen (DVB-H Untersuchung von LS telcom in Berlin). Um
//    diesen Effekt abzumildern, wird die empirische Korrektur
//    im Stadtgebiet mit einem Faktor COFA verkleinert. Der
//    Faktor hängt vom prozentualen Anteil der Bebauung entlang
//    des Profils ab, da ja nur im Stadtgebiet korrigiert werden
//    soll. Die Korrektur wird nur durchgeführt, wenn mehr als 50%
//    des Profils bebaut sind und der Empfangsort, M, oder das
//    Pixel davor, M-1, im bebauten Gebiet liegen.

    final CCHG cchg = common(CCHG.class);
    final PROF prof = common(PROF.class);
    final PBP pbp = common(PBP.class);
    
    float call(final int m, final IArr it) {
        
        final int kx = it.len();
        
        float clim = 0;
        float pcit;
        float corfa;
        int k0;
        int k1;
        int k2 = 0;

        if(cchg.ichg==2) clim=0.6F;
        if(cchg.ichg==3) clim=0.5F;
        if(cchg.ichg==4) clim=0.0F;
        
        if(m==prof.izm) {
//          Zuerst wird das komplette Profil
//          bis zum Pixel prof.izm untersucht.
            float rc=0F;
            for(int ll=2; ll<=m; ++ll) {
                float RL=(float)(ll-1);
                int itl=it.get(ll);
                if(itl>=13 && itl<=18) {
//                  Gebäude
                    rc = rc + 1;
                }
//              Prozentualer Anteil an bebautem
//              Gebiet auf dem Profil bis LL.
                pcit = rc / RL;
                if (ll<=1000){
//                  Promille werden gespeichert,
//                  aber maximal bis Pixel 1000, d.h.
//                  bis 50 km bei 50 m Auflösung.
                    pbp.ibp.set(ll, nint(1000.F * pcit));
                }
            }
        }         
        
        if(m<=1000) {
//          Prozentualer Anteil an bebautem
//          Gebiet auf dem Profil bis M.
            pcit = 0.001F * (float)(pbp.ibp.get(m));
        } else {
//          Prozentualer Anteil an bebautem
//          Gebiet auf dem Profil wird mit
//          20% angenommen.
            pcit = 0.2F;
        }

//      Morphotyp am Empfangsort, M,
//      oder am Pixel davor bei M-1.
        k0=it.get(m);
        k1=it.get(m-1);
        if(m>2) k2=it.get(m-2);
        if((k0>=13 && k0<=18) ||
                (k1>=13 && k1<=18)) {
//          Berechnung des Korrekturfaktors
            corfa= max(clim, min(1.F,2.8F-3.F*pcit));
        } else if(k2>=13 && k2<=18) {
            corfa= max(clim, min(1.F,2.8F-3.F*pcit));
            corfa=0.5F*(1.F+corfa);
        } else {
//          Empfangsort liegt nicht in
//          bebautem Gebiet.
            corfa=1.F;
        }

        return corfa;
    }
}
