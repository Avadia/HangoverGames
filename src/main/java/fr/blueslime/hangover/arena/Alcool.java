package fr.blueslime.hangover.arena;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;



public enum Alcool
{
	BEER(ChatColor.GOLD + "Bière", 1, 30,
            new String[]{
                    ChatColor.GOLD + "Rien de tel qu'une bonne bière pour se désaltérer !",
                    ChatColor.GREEN + "Faiblement alcoolisé."
            },

            new PotionEffect[]{
                    new PotionEffect(PotionEffectType.SLOW, 40 * 20, 0),
                    new PotionEffect(PotionEffectType.CONFUSION, 9 * 20, 1)
            }
    ),
	VIN(ChatColor.LIGHT_PURPLE + "Rosé", 1, 25,
            new String[]{
                    ChatColor.GOLD + "Un bon petit verre de vin !",
                    ChatColor.GREEN + "Faiblement alcoolisé"
            },

			new PotionEffect[]{
                    new PotionEffect(PotionEffectType.SLOW, 30 * 20, 0),
                    new PotionEffect(PotionEffectType.CONFUSION, 20 * 20, 1)
            }
    ),
	PASTIS(ChatColor.YELLOW + "Pastis", 2, 10,
			new String[]{
                    ChatColor.GOLD + "Un bon vieux pastis de Marseille con'",
                    ChatColor.GOLD + "Un peu plus alcoolisé"}
            ,

            new PotionEffect[]{
                    new SpecialEffect(PotionEffectType.SPEED, 2 * 20, 2, 2 * 20, 6),
                    new PotionEffect(PotionEffectType.SLOW, 40 * 20, 0),
                    new PotionEffect(PotionEffectType.CONFUSION, 20 * 20, 1)
            }
    ),
	RHUM(ChatColor.GRAY + "Rhum", 2, 5,
			new String[]{
                    ChatColor.GOLD + "" + ChatColor.ITALIC + "Du rhum, des femmes et d'la bière non de dieu !",
                    ChatColor.GOLD + "C'est du bon alcool moussaillon !"
            },

			new PotionEffect[]{
                    new SpecialEffect(PotionEffectType.SPEED, 2 * 20, 3, 2 * 20, 9),
                    new SpecialEffect(PotionEffectType.BLINDNESS, 3 * 20, 3, 2 * 20, 3),
                    new PotionEffect(PotionEffectType.SLOW, 40 * 20, 2),
                    new PotionEffect(PotionEffectType.CONFUSION, 30 * 20, 1)
            }
    ),
	WHISKY(ChatColor.GOLD + "Whisky", 3, 5,
			new String[]{
                    ChatColor.GOLD + "Un bon whisky pour se remettre les idées en place !",
                    ChatColor.AQUA + "** Plebiscité par Archibald Haddock **",
                    ChatColor.GOLD + "Très alcoolisé"
            },

			new PotionEffect[]{
                    new PotionEffect(PotionEffectType.CONFUSION, 50 * 20, 1)
            }
    ),
	ABSYNTHE(ChatColor.DARK_GREEN + "Absynthe", -3, 5,
			new String[]{
                    ChatColor.GOLD + "Alcool qui rend fou, un peu illégal.",
                    ChatColor.RED + "Ultra-méga alcoolisé - Ruine la soirée"
            },

			new PotionEffect[]{
                    new SpecialEffect(PotionEffectType.SPEED, 2 * 20, 2, 2 * 20, 6),
                    new PotionEffect(PotionEffectType.CONFUSION, 50 * 20, 1),
                    new PotionEffect(PotionEffectType.SLOW, 30 * 20, 0),
                    new PotionEffect(PotionEffectType.BLINDNESS, 15 * 20, 1)
            }
    ),
	VODKA(ChatColor.RED + "Vodka", -1, 20,
			new String[]{
                    ChatColor.GOLD + "Seul un russe peut supporter ça sans broncher.",
                    ChatColor.RED+"Trop alcoolisé - Soirée pourie"
            },

			new PotionEffect[]{
                    new SpecialEffect(PotionEffectType.SPEED, 2 * 20, 2, 2 * 20, 10),
                    new PotionEffect(PotionEffectType.SLOW, 50 * 20, 0),
                    new PotionEffect(PotionEffectType.BLINDNESS, 10 * 20, 1)
            }
    );
	
    private String name;
    private int value;
    private String[] lore;
    private int chance;
    private PotionEffect[] effects;

    Alcool(String name, int value, int chance, String[] lore, PotionEffect[] effects)
    {
        this.name = name;
        this.value = value;
        this.lore = lore;
        this.chance = chance;
        this.effects = effects;
    }

    public void applyEffects(Player p)
    {
    	for (PotionEffect effect : this.effects)
            effect.apply(p);
    }

    public ItemStack getBottle()
    {
        ItemStack bottle = new ItemStack(Material.POTION, 1);

        ArrayList<String> lore = new ArrayList<>();
        lore.addAll(this.getLore().stream().collect(Collectors.toList()));
        lore.add("");

        if (this.getValue() < 0)
            lore.add(ChatColor.RED + "Malus : " + this.getValue() + " points");
        else
            lore.add(ChatColor.GREEN + "Bonus : " + this.getValue() + " points");

        String pointsStr;

        if (this.getValue() < 0)
            pointsStr = ChatColor.RED + "-" + this.getValue()+" g/L";
        else
            pointsStr = ChatColor.GREEN + "+" + this.getValue()+" g/L";

        ItemMeta meta = bottle.getItemMeta();
        meta.setDisplayName(this.getName() + ChatColor.WHITE + " (" + pointsStr + ChatColor.WHITE + ")");
        meta.setLore(lore);
        bottle.setItemMeta(meta);

        return bottle;
    }

    public String getName() {
        return this.name;
    }

    public int getValue() {
        return this.value;
    }

    public int getChance() {
    	return this.chance;
    }
    
    public List<String> getLore() {
    	return Arrays.asList(this.lore);
    }
}
