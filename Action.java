/**
 * Një veprim i redaktuesit, i modeluar si objekt i veçantë (modeli Command).
 * Lidhet me objektivin O2 dhe kërkesën F3 të raportit.
 *
 * Objekti është i pandryshueshëm dhe ruan vetëm ndryshimin: llojin, pozicionin dhe tekstin e prekur.
 * Për DELETE teksti është ai që u fshi, që Undo ta rikthejë saktësisht.
 * Kjo është arsyeja pse historia nuk ka nevojë të ruajtë kopje të plota të dokumentit.
 */
public final class Action {

    public enum Type { INSERT, DELETE }

    private final Type type;
    private final int position;
    private final String text;

    public Action(Type type, int position, String text) {
        if (type == null) {
            throw new IllegalArgumentException("Lloji i veprimit nuk mund të jetë null.");
        }
        if (position < 0) {
            throw new IllegalArgumentException("Pozicioni nuk mund të jetë negativ.");
        }
        // Teksti bosh do të ishte veprim pa efekt (no-op); nuk lejohet të hyjë në histori.
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Teksti i veprimit nuk mund të jetë bosh.");
        }
        this.type = type;
        this.position = position;
        this.text = text;
    }

    /** Zbaton veprimin mbi dokumentin (për Redo dhe për veprimin e parë). */
    public void apply(StringBuilder doc) {
        if (type == Type.INSERT) {
            doc.insert(position, text);
        } else {
            doc.delete(position, position + text.length());
        }
    }

    /** E kundërta e apply: e kthen dokumentin siç ishte para veprimit (për Undo). */
    public void reverse(StringBuilder doc) {
        if (type == Type.INSERT) {
            doc.delete(position, position + text.length());
        } else {
            doc.insert(position, text);
        }
    }

    public Type getType() {
        return type;
    }

    public int getPosition() {
        return position;
    }

    public String getText() {
        return text;
    }

    /** Numri i karaktereve të ruajtur në këtë veprim (për matjen e memories). */
    public int size() {
        return text.length();
    }

    @Override
    public String toString() {
        return type + " pos=" + position + " \"" + text + "\"";
    }
}