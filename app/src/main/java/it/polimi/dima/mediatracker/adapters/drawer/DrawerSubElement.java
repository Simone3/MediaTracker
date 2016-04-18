package it.polimi.dima.mediatracker.adapters.drawer;

/**
 * Represents a sub-element of the navigation drawer
 */
public class DrawerSubElement
{
    private String name;
    private int icon;
    private int color;

    /**
     * Constructor
     * @param name the name of the drawer sub-element
     * @param icon the icon of the drawer sub-element
     * @param color the color of the drawer sub-element
     */
    public DrawerSubElement(String name, int icon, int color)
    {
        this.name = name;
        this.icon = icon;
        this.color = color;
    }

    /**
     * Getter
     * @return the name of the drawer sub-element
     */
    public String getName()
    {
        return name;
    }

    /**
     * Getter
     * @return the icon of the drawer sub-element
     */
    public int getIcon()
    {
        return icon;
    }

    /**
     * Getter
     * @return the color of the drawer sub-element
     */
    public int getColor()
    {
        return color;
    }
}
