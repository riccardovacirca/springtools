- modularizzare il default del backend
- integrare il frontend con il modulo status del backend
- aggiungere una cartella tmp nell'archetype con un .gitkeep
- tutti i file pid vanno messi nella cartella tmp
- tutti i file log vanno messi nella cartella logs
- esiste un file app.log egui.log nella root. verificare dove vengono generati
  e fare in modo che venga generato in logs
- se possibile anche .watch_timestamp dovrebbe stare in tmp
- rinominare il container generato mediante il file install.sh rimuovendo
  il suffisso -dev