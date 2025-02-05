(define (domain Dangeon)

    (:requirements
        :typing
        :negative-preconditions
    )

    (:types
        swords keys cells heroes
    )

    (:predicates
        ;Hero's cell location
        (at-hero ?h - heroes ?loc - cells)
        
        ;Key's cell location
        (at-key ?k -keys ?loc - cells)
        
        ;Sword cell location
        (at-sword ?s - swords ?loc - cells)
        
        ;Indicates if a cell location has a monster
        (has-monster ?loc - cells)
        
        ;Indicates if a cell location has a trap
        (has-trap ?loc - cells)
        
        ;Indicates if a cell or sword has been destroyed
        (is-destroyed ?obj)
        
        ;Indicates if a cell is locked
        (has-lock ?loc - cells)
        
        ;connects cells
        (connected ?from ?to - cells)
        
        ;Hero's hand is free
        (arm-free ?h - heroes)
        
        ;Hero's holding a Key
        (holding-key ?h - heroes ?k - keys)
        
        ;Hero's holding a sword
        (holding-sword ?h -heroes ?s - swords)
        
        ;Hero's turn
        (hero-turn ?h - heroes)
        
        ;Hero's goal cell
        (goal-cell ?h - heroes ?loc - cells)
        
        ;Indicates if a hero has reached the goal
        (reached-goal ?h - heroes)
        
    )

    ;Hero can move if the
    ;    - hero is at current location
    ;    - cells are connected, 
    ;    - there is no trap in current loc, and 
    ;    - destination does not have a trap/monster/has-been-destroyed/lock
    ;Effects move the hero, and destroy the original cell. No need to destroy the sword.
    (:action move
        :parameters (?h - heroes ?from ?to - cells)
        :precondition (and 
            (hero-turn ?h)
            (at-hero ?h ?from)
            (connected ?from ?to)
            (not (has-trap ?from))
            (not (has-trap ?to))
            (not (has-lock ?to)) ;hero do not need key to leave the lock
            (not (is-destroyed ?to))
            (not (has-monster ?to))
            (not (goal-cell ?h ?from))
        )
        :effect (and 
            (at-hero ?h ?to)
            (is-destroyed ?from)
            (not (at-hero ?h ?from))
            (not (hero-turn ?h))
            (when (goal-cell ?h ?to)   ; If the ?to location is a goal for ?h, they have finished the game
                (reached-goal ?h))
        )
    )
    
    ;When this action is executed, the hero gets into a location with a monster
    (:action move-to-monster
        :parameters (?h - heroes ?from ?to - cells ?s - swords)
        :precondition (and
            (hero-turn ?h)
            (at-hero ?h ?from)
            (connected ?from ?to)
            (has-monster ?to)
            (holding-sword ?h ?s)
            (not (has-trap ?from))
            (not (is-destroyed ?to))
            (not (goal-cell ?h ?from))
        )
        :effect (and
            (at-hero ?h ?to)
            (not (at-hero ?h ?from))
            (is-destroyed ?from)
            (not (hero-turn ?h))
            (when (goal-cell ?h ?to)   ; If the ?to location is a goal for ?h, they have finished the game
                (reached-goal ?h))
        )
    )
    
    ;When this action is executed, the hero gets into a location with a trap
    (:action move-to-trap
        :parameters (?h - heroes ?from ?to - cells)
        :precondition (and
            (hero-turn ?h)
            (at-hero ?h ?from)
            (connected ?from ?to)
            (has-trap ?to)
            (not (is-destroyed ?to))
            (not (has-trap ?from))
            (not (goal-cell ?h ?from))
        )
        :effect (and
            (at-hero ?h ?to)
            (not (at-hero ?h ?from))
            (is-destroyed ?from)
            (not (hero-turn ?h))
            (when (goal-cell ?h ?to)   ; If the ?to location is a goal for ?h, they have finished the game
                (reached-goal ?h))
        )
    )
    
    ;When this action is executed, the hero gets into a location with a trap
    (:action move-to-lock
        :parameters (?h - heroes ?from ?to - cells ?k - keys)
        :precondition (and
            (hero-turn ?h)
            (at-hero ?h ?from)
            (connected ?from ?to)
            (has-lock ?to)
            (holding-key ?h ?k)
            (not (is-destroyed ?to))
            (not (has-trap ?from))
            (not (goal-cell ?h ?from))
        )
        :effect (and
            (at-hero ?h ?to)
            (not (at-hero ?h ?from))
            (is-destroyed ?from)
            (not (hero-turn ?h))
            (when (goal-cell ?h ?to)   ; If the ?to location is a goal for ?h, they have finished the game
                (reached-goal ?h))
        )
    )
    
    (:action share-sword
        :parameters (?h1 ?h2 - heroes ?s - swords ?loc - cells)
        :precondition (and
            (at-hero ?h1 ?loc)
            (at-hero ?h2 ?loc)
            (holding-sword ?h1 ?s)
            (arm-free ?h2)
            (hero-turn ?h1)
            (not (has-trap ?loc)) ; Ensure no trap is present
        )
        :effect (and
            (not (holding-sword ?h1 ?s))
            (holding-sword ?h2 ?s)
            (arm-free ?h1)
            (not (arm-free ?h2))
            (not (hero-turn ?h1)) 
        )
    )

    (:action share-key
        :parameters (?h1 ?h2 - heroes ?k - keys ?loc - cells)
        :precondition (and
            (at-hero ?h1 ?loc)
            (at-hero ?h2 ?loc)
            (holding-key ?h1 ?k)
            (arm-free ?h2)
            (hero-turn ?h1)
            (not (has-trap ?loc)) ; Ensure no trap is present
        )
        :effect (and
            (not (holding-key ?h1 ?k))
            (holding-key ?h2 ?k)
            (arm-free ?h1)
            (not (arm-free ?h2))
            (not (hero-turn ?h1))
        )
    )

    
    ;Wait at the same location
    (:action wait
        :parameters (?h - heroes ?loc - cells)
        :precondition (and
            (hero-turn ?h)
            (at-hero ?h ?loc)
            (not (is-destroyed ?loc))
            (not (goal-cell ?h ?loc))
        )
        :effect (and
            (not (hero-turn ?h))
        )
    )
    
    ;Hero picks a key if he's in the same location
    (:action pick-key
        :parameters (?h - heroes ?loc - cells ?k - keys)
        :precondition (and
            (hero-turn ?h)
            (at-hero ?h ?loc)
            (at-key ?k ?loc)
            (arm-free ?h)
        )
        :effect (and
            (not (hero-turn ?h))
            (holding-key ?h ?k)
            (not (at-key ?k ?loc))
            (not (arm-free ?h))
        )
    )
    
    ;Hero destroys his sword. 
    (:action destroy-key
        :parameters (?h - heroes ?loc - cells ?k - keys)
        :precondition (and 
            (hero-turn ?h)
            (holding-key ?h ?k)
            (at-hero ?h ?loc)
            (not (has-trap ?loc))          
        )
        :effect (and
            (is-destroyed ?k)
            (not (holding-key ?h ?k))
            (not (hero-turn ?h))
            (arm-free ?h)        
        )
    )
    
    ;Hero picks a sword if he's in the same location
    (:action pick-sword
        :parameters (?h - heroes ?loc - cells ?s - swords)
        :precondition (and
            (hero-turn ?h)
            (at-hero ?h ?loc)
            (at-sword ?s ?loc)
            (arm-free ?h)
        )
        :effect (and
            (not (hero-turn ?h))
            (holding-sword ?h ?s)
            (not (at-sword ?s ?loc))
            (not (arm-free ?h))
        )
    )
    
    ;Hero destroys his sword. 
    (:action destroy-sword
        :parameters (?h - heroes ?loc - cells ?s - swords)
        :precondition (and 
            (hero-turn ?h)
            (holding-sword ?h ?s)
            (at-hero ?h ?loc)
            (not (has-monster ?loc))   
            (not (has-trap ?loc))          
        )
        :effect (and
            (is-destroyed ?s)
            (not (holding-sword ?h ?s))
            (not (hero-turn ?h))
            (arm-free ?h)        
        )
    )
    
    ;Hero disarms the trap with his free arm
    (:action disarm-trap
        :parameters (?h - heroes ?loc - cells)
        :precondition (and
            (at-hero ?h ?loc)
            (has-trap ?loc)
            (hero-turn ?h)
            (arm-free ?h)
        )
        :effect (and
            (not (hero-turn ?h))
            (not (has-trap ?loc))
        )
    )
    
    ;When every hero finished their turn, finish a round
    (:action finish-round
        :parameters ()
        :precondition (and
            (forall (?h - heroes) (or (not (hero-turn ?h)) (reached-goal ?h))) ; All heroes have either finished their turn or reached the goal
        )
        :effect (and
            ; Reset the hero-turn status for the next round
            (forall (?h - heroes)
                (when (not (reached-goal ?h))
                    (hero-turn ?h)
                )
            )
        )
    )

)