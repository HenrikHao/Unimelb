import bagel.util.Rectangle;

/**
 * this is an interface for attacking between entities
 */
public interface Attackable {

    /**
     *
     * @param attackRec attacker's attack range
     * @param damageRec rectangle of entities being attacked
     * @return true if attackRec collides with damageRec
     */
    boolean attack(Rectangle attackRec, Rectangle damageRec);
}
