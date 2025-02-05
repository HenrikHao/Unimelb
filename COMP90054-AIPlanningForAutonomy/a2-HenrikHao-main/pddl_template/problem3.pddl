;It's recommended to install the misc-pddl-generators plugin 
;and then use Network generator to create the graph
(define (problem p3-dangeon)
  (:domain Dangeon)
  (:objects
            cell1_1 cell1_2 cell1_3 cell1_4 cell1_5 
            cell2_1 cell2_2 cell2_3 cell2_4 cell2_5 
            cell3_1 cell3_2 cell3_3 cell3_4 cell3_5 
            cell4_1 cell4_2 cell4_3 cell4_4 cell4_5 - cells
            sword1 sword2 sword3 sword4 - swords
  )
  (:init
  
    ;Initial Hero Location
    (at-hero cell4_5)
    
    ;He starts with a free arm
    (arm-free)
    
    ;Initial location of the swords
    (at-sword sword1 cell1_5)
    (at-sword sword2 cell2_3)
    (at-sword sword3 cell3_1)
    (at-sword sword4 cell4_3)
    
    ;Initial location of Monsters
    (has-monster cell1_3)
    (has-monster cell1_4)
    (has-monster cell2_2)
    (has-monster cell3_2)
    (has-monster cell3_4)
    (has-monster cell4_2)
    (has-monster cell4_4)
    
    ;Initial location of Traps
    (has-trap cell1_2)
    (has-trap cell2_1)
    (has-trap cell2_4)
    (has-trap cell2_5)
    (has-trap cell3_3)
    (has-trap cell4_1)
    
    ;Graph Connectivity
    (Connected cell1_1 cell1_2)
    (Connected cell1_2 cell1_1)
    (Connected cell1_2 cell1_3)
    (Connected cell1_3 cell1_2)
    (Connected cell1_3 cell1_4)
    (Connected cell1_4 cell1_3)
    (Connected cell1_4 cell1_5)
    (Connected cell1_5 cell1_4)
    (Connected cell2_1 cell2_2)
    (Connected cell2_2 cell2_1)
    (Connected cell2_2 cell2_3)
    (Connected cell2_3 cell2_2)
    (Connected cell2_3 cell2_4)
    (Connected cell2_4 cell2_3)
    (Connected cell2_4 cell2_5)
    (Connected cell2_5 cell2_4)
    (Connected cell3_1 cell3_2)
    (Connected cell3_2 cell3_1)
    (Connected cell3_2 cell3_3)
    (Connected cell3_3 cell3_2)
    (Connected cell3_3 cell3_4)
    (Connected cell3_4 cell3_3)
    (Connected cell3_4 cell3_5)
    (Connected cell3_5 cell3_4)
    (Connected cell4_1 cell4_2)
    (Connected cell4_2 cell4_1)
    (Connected cell4_2 cell4_3)
    (Connected cell4_3 cell4_2)
    (Connected cell4_3 cell4_4)
    (Connected cell4_4 cell4_3)
    (Connected cell4_4 cell4_5)
    (Connected cell4_5 cell4_4)
    (Connected cell1_1 cell2_1)
    (Connected cell2_1 cell1_1)
    (Connected cell1_2 cell2_2)
    (Connected cell2_2 cell1_2)
    (Connected cell1_3 cell2_3)
    (Connected cell2_3 cell1_3)
    (Connected cell1_4 cell2_4)
    (Connected cell2_4 cell1_4)
    (Connected cell1_5 cell2_5)
    (Connected cell2_5 cell1_5)
    (Connected cell2_1 cell3_1)
    (Connected cell3_1 cell2_1)
    (Connected cell2_2 cell3_2)
    (Connected cell3_2 cell2_2)
    (Connected cell2_3 cell3_3)
    (Connected cell3_3 cell2_3)
    (Connected cell2_4 cell3_4)
    (Connected cell3_4 cell2_4)
    (Connected cell2_5 cell3_5)
    (Connected cell3_5 cell2_5)
    (Connected cell3_1 cell4_1)
    (Connected cell4_1 cell3_1)
    (Connected cell3_2 cell4_2)
    (Connected cell4_2 cell3_2)
    (Connected cell3_3 cell4_3)
    (Connected cell4_3 cell3_3)
    (Connected cell3_4 cell4_4)
    (Connected cell4_4 cell3_4)
    (Connected cell3_5 cell4_5)
    (Connected cell4_5 cell3_5)
  )
  (:goal (and
            ;Hero's Goal Location
            (at-hero cell1_1)
  ))
  
)
