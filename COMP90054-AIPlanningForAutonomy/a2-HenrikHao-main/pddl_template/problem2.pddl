(define (problem p2-dangeon)
  (:domain Dangeon)
  (:objects
            cell1 cell2 cell3 cell4 cell5 cell6 cell7 cell8 cell9 cell10 cell11 cell12 - cells
            sword1 sword2 - swords
  )
  (:init
    ;Initial Hero Location
    (at-hero cell7)
    
    ;He starts with a free arm
    (arm-free)
    
    ;Initial location of the swords
    (at-sword sword1 cell10)
    (at-sword sword2 cell6)
    
    ;Initial location of Monsters
    (has-monster cell4)
    (has-monster cell5)
    (has-monster cell9)
    
    ;Initial location of Traps
    (has-trap cell2)
    (has-trap cell3)
    (has-trap cell8)
    (has-trap cell11)
    (has-trap cell12)
    
    ;Graph Connectivity
    (Connected cell1 cell2)
    (Connected cell2 cell1)
    (Connected cell2 cell3)
    (Connected cell3 cell2)
    (Connected cell3 cell4)
    (Connected cell4 cell3)
    (Connected cell4 cell5)
    (Connected cell5 cell4)
    (Connected cell5 cell6)
    (Connected cell6 cell5)
    (Connected cell6 cell7)
    (Connected cell7 cell6)
    (Connected cell1 cell8)
    (Connected cell8 cell1)
    (Connected cell8 cell9)
    (Connected cell9 cell8)
    (Connected cell9 cell10)
    (Connected cell10 cell9)
    (Connected cell10 cell11)
    (Connected cell11 cell10)
    (Connected cell11 cell12)
    (Connected cell12 cell11)
    (Connected cell12 cell7)
    (Connected cell7 cell12)
    (Connected cell2 cell9)
    (Connected cell9 cell2)
    (Connected cell3 cell8)
    (Connected cell8 cell3)
    (Connected cell3 cell10)
    (Connected cell10 cell3)
    (Connected cell4 cell9)
    (Connected cell9 cell4)
    (Connected cell4 cell11)
    (Connected cell11 cell4)
    (Connected cell5 cell10)
    (Connected cell10 cell5)
    (Connected cell5 cell12)
    (Connected cell12 cell5)
    (Connected cell6 cell11)
    (Connected cell11 cell6)
  )
  (:goal (and
            ;Hero's Goal Location
            (at-hero cell1)
  ))
)