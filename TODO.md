- modularizzare il default del backend
- integrare il frontend con il modulo status del backend
- spostare release.sh in bin/release
- al termine della installazione dell'ambiente clonare springtools col nome
  originale nella root del progetto per evolutive e rimuovere .git dalla root
  del progetto corrente
- aggiungere un comando con conferma a cmd per sincronizzare springtools alla
  versione locale: cmd repo sync -r `<repo>`. questo comando deve caricare un
  file di configurazione della sincronizzazione da `<repo>` ed eseguire rsync
  dall'origine usando la configurazione per settare gli argomenti del comando
  rsync. es: $include, $exclude, $orig, $dest. il presupposto è che i file da
  sincronizzare si trovino nello stesso path relativo
