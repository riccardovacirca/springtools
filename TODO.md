# TODO

- [x] ~~il file .toolchain/archetype/src/main/resources/archetype-resources/.gitignore
  della app non viene copiato nella root del progetto durante l'installazione
  sovrascrivendo il file .gitignore del repo di origine.~~
  **RISOLTO**: Modificato install.sh per usare `mv -f` e forzare la sovrascrittura
  dei file nascosti incluso .gitignore (linee 589, 593).

- [x] ~~Modificare i nomi delle migration nella app e nell'archetipo in modo che ogni
  migration sia relativa a un modulo e sia il più possibile indipendente.~~
  **RISOLTO**: Adottato il formato V<timestamp>__module_<nome_modulo>.sql
  - Migration rinominata da V1__init_database.sql a V20260101_120000__module_status.sql
  - Il prefisso "module_" permette di distinguere migration modulari da quelle generiche
  - Il timestamp deve essere scritto manualmente dallo sviluppatore nel formato yyyymmdd_hhmmss