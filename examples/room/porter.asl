+!locked(door)[source(paranoid)]
  : ~locked(door)
  <- lock.
  
+!~locked(door)[source(claustrophobe)]
  : locked(door)
  <- unlock.
