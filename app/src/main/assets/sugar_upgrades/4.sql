alter table MOVIE add order_in_importance_level int;
alter table BOOK add order_in_importance_level int;
alter table TV_SHOW add order_in_importance_level int;
alter table VIDEOGAME add order_in_importance_level int;
update MOVIE set order_in_importance_level=0;
update BOOK set order_in_importance_level=0;
update TV_SHOW set order_in_importance_level=0
update VIDEOGAME set order_in_importance_level=0;
/* NOT WORKING!  Failed to read row 12, column -1 from a CursorWindow which has 19 rows, 18 columns. */