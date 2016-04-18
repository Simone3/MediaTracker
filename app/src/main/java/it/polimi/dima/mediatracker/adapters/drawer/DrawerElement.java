package it.polimi.dima.mediatracker.adapters.drawer;

import java.util.List;

/**
 * Represents an element of the navigation drawer
 */
public class DrawerElement
{
    private String name;
    private int icon;
    private int color;
    private List<DrawerSubElement> subItems;

    /**
     * Constructor
     * @param name the name of the drawer element
     * @param icon the icon of the drawer element
     * @param color the color of the drawer element
     * @param subItems the sub-elements of this element (null if none)
     */
    public DrawerElement(String name, int icon, int color, List<DrawerSubElement> subItems)
    {
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.subItems = subItems;
    }

    /**
     * Getter
     * @return the name of the drawer element
     */
    public String getName()
    {
        return name;
    }

    /**
     * Getter
     * @return the icon of the drawer element
     */
    public int getIcon()
    {
        return icon;
    }

    /**
     * Getter
     * @return the color of the drawer element
     */
    public int getColor()
    {
        return color;
    }

    /**
     * Getter
     * @return the sub-elements of this element (null if none)
     */
    public List<DrawerSubElement> getSubElements()
    {
        return subItems;
    }
}
