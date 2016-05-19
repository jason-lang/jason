!start.

+!start 
   <- .send(bob,tell,happy(bob));
      .send(bob,tell,happy(alice1));
      .send(bob,tell,happy(alice2));
      .send(bob,tell,happy(alice3));
      .send(bob,tell,happy(alice4));
      .wait(1000);
      .send(bob,tell,happy(morgana));
      .wait(1000);
      /*.send(bob,tell,today(wednesday));
      .wait(3000);
      .send(bob,tell,today(friday));
      .wait(3000);
      .send(bob,tell,today(saturday));
      */
      //.send(bob,untell,happy(bob));
      .wait(1000);
      .send(bob,untell,happy(alice)).
