;It's recommended to install the misc-pddl-generators plugin 
;and then use Network generator to create the graph
(define (problem multi-heroes)
  (:domain Dangeon)
  (:objects
            cell1_1 cell1_2 cell1_3 cell1_4 cell1_5 
            cell2_1 cell2_2 cell2_3 cell2_4 cell2_5 
            cell3_1 cell3_2 cell3_3 cell3_4 cell3_5 
            cell4_1 cell4_2 cell4_3 cell4_4 cell4_5 - cells
            hero1 hero2 hero3 - heroes
            sword1 - swords
            key1 key2 - keys
  )
  (:init
  
    ;Initial Hero Location
    (at-hero hero1 cell1_1)
    (at-hero hero2 cell4_5)
    (at-hero hero3 cell2_2)
    
    ;Heroes start with a free arm
    (arm-free hero1)
    (arm-free hero2)
    (arm-free hero3)
    
    ;Hero Goal location
    (goal-cell hero1 cell4_4)
    (goal-cell hero2 cell1_3)
    (goal-cell hero3 cell3_4)
    
    ;Initialize Hero turn
    (hero-turn hero1)
    (hero-turn hero2)
    (hero-turn hero3)
    
    ;Initial location of the keys
    (at-key key1 cell4_2)
    (at-key key2 cell2_3)
    
    ;Initial location of the swords
    (at-sword sword1 cell3_1)
    
    ;Initial location of Monsters
    (has-monster cell4_1)
    (has-monster cell1_5)
    
    ;Initial location of Traps
    (has-trap cell2_4)
    (has-trap cell1_3)
    (has-trap cell2_1)
    (has-trap cell3_3)
    
    ;Initial location of Locks
    (has-lock cell3_2)
    (has-lock cell4_3)
    (has-lock cell1_4)
    (has-lock cell1_2)
    
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
            (at-hero hero1 cell4_4)
            (at-hero hero2 cell1_3)
            (at-hero hero3 cell3_4)
  ))
  
)

