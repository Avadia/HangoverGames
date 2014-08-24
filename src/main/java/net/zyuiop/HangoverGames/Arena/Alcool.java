package net.zyuiop.HangoverGames.Arena;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;



public enum Alcool {
	Bierre(ChatColor.GOLD+"Bière", 1, 30, 
			new String[]{ChatColor.GOLD+"Rien de tel qu'une bonne bière pour se désaltérer !", ChatColor.GREEN+"Faiblement alcoolisé."},
			new PotionEffect[]{new PotionEffect(PotionEffectType.SLOW, 40*20, 0), new PotionEffect(PotionEffectType.CONFUSION, 9*20, 1)}),
	Vin(ChatColor.LIGHT_PURPLE+"Rosé", 1, 25, 
			new String[]{ChatColor.GOLD+"Un bon petit verre de vin !", ChatColor.GREEN+"Faiblement alcoolisé"},
			new PotionEffect[]{new PotionEffect(PotionEffectType.SLOW, 30*20, 0), new PotionEffect(PotionEffectType.CONFUSION, 20*20, 1)}),
	Pastis(ChatColor.YELLOW+"Pastis", 2, 10, 
			new String[]{ChatColor.GOLD+"Un bon vieux pastis de Marseille con'", ChatColor.GOLD+"Un peu plus alcoolisé"},
			new PotionEffect[]{new SpecialEffect(PotionEffectType.SPEED, 2*20, 2, 2*20, 6), new PotionEffect(PotionEffectType.SLOW, 40*20, 0), new PotionEffect(PotionEffectType.CONFUSION, 20*20, 1)}),
	Rhum(ChatColor.GRAY+"Rhum", 2, 5, 
			new String[]{ChatColor.GOLD+""+ChatColor.ITALIC+"Du rhum des femmes et d'la bière non de dieu !", ChatColor.GOLD+"C'est du bon alcool moussaillon !"},
			new PotionEffect[]{new SpecialEffect(PotionEffectType.SPEED, 2*20, 3, 2*20, 9), new SpecialEffect(PotionEffectType.BLINDNESS, 3*20, 3, 2*20, 3), new PotionEffect(PotionEffectType.SLOW, 40*20, 2), new PotionEffect(PotionEffectType.CONFUSION, 30*20, 1)}),
	Whisky(ChatColor.GOLD+"Whisky", 3, 5, 
			new String[]{ChatColor.GOLD+"Un bon whisky pour se remettre les idées en place !", ChatColor.AQUA+"** Plebiscité par Archibald Haddock **",  ChatColor.GOLD+"Très alcoolisé"},
			new PotionEffect[]{new PotionEffect(PotionEffectType.CONFUSION, 50*20, 1)}),
	Absinthe(ChatColor.DARK_GREEN+"Absynthe", -3, 5,
			new String[]{ChatColor.GOLD+"Alcool qui rend fou, un peu illégal.", ChatColor.RED+"Ultra-méga alcoolisé - Ruine la soirée"},
			new PotionEffect[]{new SpecialEffect(PotionEffectType.SPEED, 2*20, 2, 2*20, 6), new PotionEffect(PotionEffectType.CONFUSION, 50*20, 1), new PotionEffect(PotionEffectType.SLOW, 30*20, 0), new PotionEffect(PotionEffectType.BLINDNESS, 15*20, 1)}),
	Vodka(ChatColor.RED+"Vodka", -1, 20,
			new String[]{ChatColor.GOLD+"Seul un russe peut supporter ça sans broncher.", ChatColor.RED+"Trop alcoolisé - Soirée pourie"},
			new PotionEffect[]{new SpecialEffect(PotionEffectType.SPEED, 2*20, 2, 2*20, 10), new PotionEffect(PotionEffectType.SLOW, 50*20, 0), new PotionEffect(PotionEffectType.BLINDNESS, 10*20, 1)});
	
    private String nom;
    private int value;
    private String[] lore;
    private int chance;
    private PotionEffect[] effects;

    private Alcool(String nom, int value, int chance, String[] lore, PotionEffect[] effects) {
        this.nom = nom;
        this.value = value;
        this.lore = lore;
        this.chance = chance;
        this.effects = effects;
    }
    
    public PotionEffect[] getEffects() {
    	return effects;
    }
    
    public void applyEffects(Player p) {
    	for (PotionEffect e : effects) {
    		e.apply(p);
    	}
    }

    public String getNom() {
        return nom;
    }

    public int getValue() {
        return value;
    }

    public int getChance() {
    	return chance;
    }
    
    public List<String> getLore() {
    	return Arrays.asList(lore);
    }
}
