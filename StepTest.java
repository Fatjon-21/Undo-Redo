/**
 * Testi i këtij hapi: Action, strukturat undoStack/redoStack dhe addAction.
 * Ekzekutimi: javac -d out *.java   pastaj   java -cp out StepTest
 */
public class StepTest {

    static int passed = 0;
    static int failed = 0;

    static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("[PASS] " + name);
        } else {
            failed++;
            System.out.println("[FAIL] " + name);
        }
    }

    public static void main(String[] args) {
        // F3: Action ruan llojin, pozicionin dhe tekstin e prekur
        Action a1 = new Action(Action.Type.INSERT, 0, "Pershendetje");
        check("F3 Action ruan llojin, pozicionin dhe tekstin",
                a1.getType() == Action.Type.INSERT && a1.getPosition() == 0
                        && a1.getText().equals("Pershendetje"));
        check("F3 Action refuzon lloj null", rejects(() -> new Action(null, 0, "x")));
        check("F3 Action refuzon pozicion negativ",
                rejects(() -> new Action(Action.Type.INSERT, -1, "x")));
        check("F3 Action refuzon tekst bosh ose null",
                rejects(() -> new Action(Action.Type.INSERT, 0, ""))
                        && rejects(() -> new Action(Action.Type.INSERT, 0, null)));

        // F3: apply dhe reverse janë të kundërta për të dy llojet
        StringBuilder doc = new StringBuilder("abcdef");
        Action ins = new Action(Action.Type.INSERT, 3, "XY");
        ins.apply(doc);
        boolean afterInsert = doc.toString().equals("abcXYdef");
        ins.reverse(doc);
        check("F3 INSERT: apply pastaj reverse rikthen tekstin",
                afterInsert && doc.toString().equals("abcdef"));
        Action del = new Action(Action.Type.DELETE, 1, "bc");
        del.apply(doc);
        boolean afterDelete = doc.toString().equals("adef");
        del.reverse(doc);
        check("F3 DELETE: apply pastaj reverse rikthen tekstin e fshirë",
                afterDelete && doc.toString().equals("abcdef"));

        // F4: dy stack-e LIFO, fillimisht bosh
        UndoRedoManager m = new UndoRedoManager();
        check("F4 stack-et fillojnë bosh", m.undoSize() == 0 && m.redoSize() == 0
                && m.peekUndo() == null && m.peekRedo() == null);

        Action a2 = new Action(Action.Type.INSERT, 12, " bote");
        Action a3 = new Action(Action.Type.DELETE, 12, " bote");

        m.addAction(a1);
        check("F4 pas veprimit 1 maja e undoStack është veprimi 1", m.peekUndo() == a1);
        m.addAction(a2);
        check("F4 pas veprimit 2 maja është veprimi 2 (LIFO)", m.peekUndo() == a2);
        m.addAction(a3);
        check("F4 pas veprimit 3 maja është veprimi 3 (LIFO)", m.peekUndo() == a3);
        check("F4 undoStack ka 3 veprime", m.undoSize() == 3);
        check("F4 redoStack mbetet bosh pas shtimit të veprimeve", m.redoSize() == 0 && m.peekRedo() == null);

        // F4: veprimi i ri pas Undo-së e pastron Redo-n (kjo provë ka kuptim vetëm kur Redo nuk është bosh)
        m.pushRedo(m.popUndo());
        check("F4 pas një Undo, redoStack ka veprimin e zhbërë",
                m.redoSize() == 1 && m.peekRedo() == a3 && m.undoSize() == 2);
        Action a4 = new Action(Action.Type.INSERT, 0, "X");
        m.addAction(a4);
        check("F4 addAction pas Undo-së e pastron redoStack", m.redoSize() == 0 && m.peekUndo() == a4);

        // F5: veprim null refuzohet pa ndryshuar historinë
        check("F5 veprim null refuzohet", rejects(() -> m.addAction(null)));
        check("F5 pas refuzimit historia është e pandryshuar", m.undoSize() == 3 && m.peekUndo() == a4);

        // ==================== KODI I Z: fillon (testet e Redo) ====================
        // F6: Redo (pika Z). Undo simulohet me primitivat e menaxherit, pa varësi nga funksioni Undo i pikës Y.
        UndoRedoManager rm = new UndoRedoManager();
        StringBuilder rd = new StringBuilder();
        Action r1 = new Action(Action.Type.INSERT, 0, "A");
        Action r2 = new Action(Action.Type.INSERT, 1, "B");
        r1.apply(rd);
        rm.addAction(r1);
        r2.apply(rd);
        rm.addAction(r2);

        check("F6 Redo me redoStack bosh kthen null dhe nuk e ndryshon tekstin",
                rm.redo(rd) == null && rd.toString().equals("AB") && rm.undoSize() == 2);

        Action undone = rm.popUndo();
        undone.reverse(rd);
        rm.pushRedo(undone);
        boolean afterUndo = rd.toString().equals("A");
        Action redone = rm.redo(rd);
        check("F6 Redo rikthen veprimin e zhbërë (A -> AB)",
                afterUndo && redone == r2 && rd.toString().equals("AB"));
        check("F6 pas Redo veprimi kthehet në undoStack dhe redoStack zbrazet",
                rm.peekUndo() == r2 && rm.undoSize() == 2 && rm.redoSize() == 0);

        // Dy Undo pastaj dy Redo: veprimet rikthehen në radhën e duhur (LIFO)
        for (int i = 0; i < 2; i++) {
            Action u = rm.popUndo();
            u.reverse(rd);
            rm.pushRedo(u);
        }
        boolean empty = rd.length() == 0;
        rm.redo(rd);
        boolean afterFirstRedo = rd.toString().equals("A");
        rm.redo(rd);
        check("F6 dy Undo dhe dy Redo rikthejnë tekstin në radhën e duhur",
                empty && afterFirstRedo && rd.toString().equals("AB"));

        // Undo, Redo, Undo: veprimi i rikthyer mund të zhbëhet përsëri
        Action again = rm.popUndo();
        again.reverse(rd);
        rm.pushRedo(again);
        check("F6 Undo pas Redo funksionon (AB -> A)", rd.toString().equals("A") && rm.redoSize() == 1);

        // Veprim i ri pas Undo-së e pastron Redo-n, pra Redo nuk bën asgjë
        Action r3 = new Action(Action.Type.INSERT, 1, "C");
        r3.apply(rd);
        rm.addAction(r3);
        check("F6 Redo pas veprimi të ri nuk bën asgjë",
                rm.redo(rd) == null && rd.toString().equals("AC"));

        // ==================== KODI I Z: mbaron (testet e Redo) ====================

        // F7: kufiri i thellësisë
        UndoRedoManager limited = new UndoRedoManager(2);
        limited.addAction(a1);
        limited.addAction(a2);
        limited.addAction(a3);
        check("F7 me maxDepth=2 historia ka vetëm 2 veprimet e fundit",
                limited.undoSize() == 2 && limited.peekUndo() == a3 && limited.undoActions().get(0) == a2);
        check("F7 maxDepth negativ refuzohet", rejects(() -> new UndoRedoManager(-1)));

        // F8: treguesi i memories numëron vetëm tekstin e ruajtur
        long expected = a1.getText().length() + a2.getText().length() + a4.getText().length();
        check("F8 storedChars numëron karakteret e ruajtur (" + expected + ")", m.storedChars() == expected);

        System.out.println();
        System.out.println("Rezultati: " + passed + " kaluan, " + failed + " dështuan.");
        if (failed > 0) {
            System.exit(1);
        }
    }

    static boolean rejects(Runnable r) {
        try {
            r.run();
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }
}