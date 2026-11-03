# Undo-Redo

Redaktues teksti me histori veprimesh (Undo dhe Redo), i shkruar në Java. Historia ruhet me dy stack-e (LIFO), dhe çdo veprim modelohet si objekt i veçantë (modeli Command).

## Pika Z: Redo

Funksioni Redo rikthen veprimin e fundit të zhbërë.

### Ndryshimet

- **UndoRedoManager.java:** shtohet metoda `redo(StringBuilder doc)`. Merr veprimin nga `redoStack`, e kryen përsëri mbi dokumentin dhe e kthen në `undoStack` me `pushUndo`. Kthen veprimin e rikthyer, ose `null` kur nuk ka çfarë të rikthehet.
- **StepTest.java:** 6 teste të reja F6:
  - Redo me histori bosh
  - Redo pas Undo
  - dy Undo dhe dy Redo në radhën e duhur
  - Undo pas Redo
  - Redo pas një veprimi të ri
- Shtohen komente që ndajnë kodin e X dhe të Z. Kodi ekzistues nuk është ndryshuar.

### Rezultati i testeve

25 kaluan, 0 dështuan.

## Si ta ekzekutosh

```
javac -d out Action.java StepTest.java UndoRedoManager.java
java -cp out StepTest
```
````
