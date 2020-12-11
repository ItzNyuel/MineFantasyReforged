package minefantasy.mfr.recipe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import minefantasy.mfr.api.crafting.carpenter.CraftingManagerCarpenter;
import minefantasy.mfr.api.crafting.carpenter.CustomToolRecipeCarpenter;
import minefantasy.mfr.api.crafting.carpenter.ICarpenterRecipe;
import minefantasy.mfr.api.crafting.carpenter.ShapedCarpenterRecipes;
import net.minecraft.item.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Class which can be used to serialize jsons from the recipes in the MFR recipe registry
 */
public class RecipeExporter {

	public RecipeExporter() {
		exportRecipes();
	}

	public void exportRecipes() {

		CraftingManagerCarpenter instance = CraftingManagerCarpenter.getInstance();
		List recipes = instance.recipes;

		Gson g = new GsonBuilder().setPrettyPrinting().create();

		for (int r = 0; r < recipes.size(); r++) {

			Object recipe = recipes.get(r);
			ICarpenterRecipe iCarpenterRecipe = (ICarpenterRecipe) recipe;

			//// Properties ////

			boolean is_tool_recipe = recipe instanceof CustomToolRecipeCarpenter;

			String type = recipe instanceof ShapedCarpenterRecipes ? "crafting_shaped" : "crafting_shapeless";

			int craft_time = 5;
			try { craft_time = iCarpenterRecipe.getCraftTime(); } catch (Exception e) {}

			String skill = "none";
			try { skill = iCarpenterRecipe.getSkill().skillName; } catch (Exception e) {}

			String tool_type = "none";
			try { tool_type = iCarpenterRecipe.getToolType(); } catch (Exception e) {}

			int experience = 0;
			try { experience = (int) iCarpenterRecipe.getExperience(); } catch (Exception e) {}

			String sound = "none";
			try { sound = iCarpenterRecipe.getSound().getSoundName().toString(); } catch (Exception e) {}

			String research = "none";
			try { research = iCarpenterRecipe.getResearch(); } catch (Exception e) {}

			int block_tier = -1;
			try { block_tier = iCarpenterRecipe.getBlockTier(); } catch (Exception e) {}

			int recipe_hammer = 0;
			try { recipe_hammer = iCarpenterRecipe.getRecipeHammer(); } catch (Exception e) {}

			int recipeWidth = ((ShapedCarpenterRecipes) recipe).recipeWidth;
			int recipeHeight = ((ShapedCarpenterRecipes) recipe).recipeHeight;

			ItemStack[] recipeItems = ((ShapedCarpenterRecipes) recipe).recipeItems;

			List<String> recipeUniqueIngredientItemList = new ArrayList<>();
			List<ItemStack> recipeItemsList = new ArrayList<>();

			for (ItemStack stack : recipeItems) {
				recipeItemsList.add(stack);

				String itemName = stack.getItem().getRegistryName().toString();
				if (!recipeUniqueIngredientItemList.contains(itemName)) {
					recipeUniqueIngredientItemList.add(itemName);
				}
			}

			Map<Character, String> keyMap = new HashMap<>();

			if (recipeUniqueIngredientItemList.contains("minefantasyreborn:guts")) {
				System.out.println("stop here");
			}

			for (String item : recipeUniqueIngredientItemList) {

				String trimmed = stripNamespace(item);

				if (!keyMap.keySet().contains(trimmed.toUpperCase().charAt(0))) {
					keyMap.put(trimmed.toUpperCase().charAt(0), item);
				} else if ((trimmed.length() > 1 && !keyMap.keySet().contains(trimmed.toUpperCase().charAt(1)))) {
					keyMap.put(trimmed.toUpperCase().charAt(1), item);
				} else if ((trimmed.length() > 2 && !keyMap.keySet().contains(trimmed.toUpperCase().charAt(2)))) {
					keyMap.put(trimmed.toUpperCase().charAt(2), item);
				} else if ((trimmed.length() > 3 && !keyMap.keySet().contains(trimmed.toUpperCase().charAt(3)))) {
					keyMap.put(trimmed.toUpperCase().charAt(3), item);
				} else if ((trimmed.length() > 4 && !keyMap.keySet().contains(trimmed.toUpperCase().charAt(4)))) {
					keyMap.put(trimmed.toUpperCase().charAt(4), item);
				} else if ((trimmed.length() > 5 && !keyMap.keySet().contains(trimmed.toUpperCase().charAt(5)))) {
					keyMap.put(trimmed.toUpperCase().charAt(5), item);
				} else {
					boolean foundKey = false;
					while (!foundKey) {
						Character character = Character.toUpperCase(getRandomChar());
						if (!keyMap.keySet().contains(character)) {
							keyMap.put(Character.toUpperCase(character), item);
							foundKey = true;
						}

					}
				}
			}

			Map<String, Character> reverseKeyMap = new HashMap<>();
			for (Map.Entry<Character, String> entry : keyMap.entrySet()) {
				reverseKeyMap.put(entry.getValue(), entry.getKey());
			}

			String[] pattern = {"    ", "    ", "    ", "    ",};

			List<ItemStack> list = new ArrayList<>();

			Collections.addAll(list, recipeItems);

			Iterator iterator = list.iterator();
			for (int row = 0; row < recipeHeight; row++) {

				for (int column = 0; column < recipeWidth; column++) {

					if (!iterator.hasNext()) {
						System.out.println("this shouldn't happen");
					}

					ItemStack itemStack = (ItemStack) iterator.next();
					if (itemStack.isEmpty()) {
						continue;
					}
					String curritem = (itemStack.getItem().getRegistryName().toString());
					Character character = reverseKeyMap.get(curritem);
					if (character == null) {
						System.out.println("stop here");
					}

					char[] myNameChars = pattern[row].toCharArray();
					myNameChars[column] = character;
					pattern[row] = String.valueOf(myNameChars);
				}
			}

			Map<String, Object> result = new HashMap<>();

			//// Result ////

			// result item
			result.put("item", iCarpenterRecipe.getRecipeOutput().getItem().getRegistryName().toString());

			// result count, if greater than 1
			if (iCarpenterRecipe.getRecipeOutput().getCount() > 1) {
				result.put("count", (iCarpenterRecipe.getRecipeOutput().getCount()));
			}

			// result's nbt
			if (iCarpenterRecipe.getRecipeOutput().hasTagCompound()) {
				Object nbt = iCarpenterRecipe.getRecipeOutput().getTagCompound();
				result.put("nbt", nbt);
			}

			//// construct Key ////
			Map<Character, Map<String, Object>> key = new HashMap<>();
			for (Map.Entry entry : keyMap.entrySet()) {

				if (entry.getValue().equals("minecraft:air")) {
					continue;
				}

				//				Map<Character,  Map<String, String>> characterStringMap = new HashMap<>();
				Map<String, Object> subMap = new HashMap<>();
				subMap.put("item", entry.getValue());

				for (ItemStack stack : recipeItemsList) {
					if (stack.getItem().getRegistryName().toString().equals(entry.getValue()) && stack.getItemDamage() != 0 && stack.getItemDamage() != 32767) {
						subMap.put("data", (stack.getItemDamage()));
						if (stack.hasTagCompound()) {
							subMap.put("nbt", stack.getTagCompound());
						}
						break;
					}
				}

				//				characterStringMap.put((Character) entry.getKey(), subMap);
				key.put((Character) entry.getKey(), subMap);
			}

			//// Writing Properties to new obj ////
			TempRecipe tempRecipe = new TempRecipe();

			tempRecipe.type = type;
			tempRecipe.skill = skill;
			tempRecipe.research = research;
			tempRecipe.tool_type = tool_type;
			tempRecipe.is_tool_recipe = is_tool_recipe;
			tempRecipe.block_tier = block_tier;
			tempRecipe.craft_time = craft_time;
			tempRecipe.recipe_hammer = recipe_hammer;
			tempRecipe.experience = experience;
			tempRecipe.sound = sound;
			tempRecipe.pattern = pattern;

			//		tempRecipe.key = key;
			tempRecipe.result = result;

			// just in case..
			//g.toJson(tempRecipe);

			tempRecipe.key = key;

			String fileName = iCarpenterRecipe.getRecipeOutput().getItem().getRegistryName().toString();
			String trimmedName = stripNamespace(fileName);

			// Java objects to File
			//		for (int m = 0; m < 3; i++) {

			// check if file already exists
			File tmp = new File("C:\\git\\MineFantasy-reborn\\src\\main\\resources\\assets\\minefantasyreborn\\generated_recipes\\" + trimmedName + ".json");
			boolean exists = tmp.exists();

			if (exists) {
				int index = 1;
				while (exists) {
					index++;
					File tmpFile = new File("C:\\git\\MineFantasy-reborn\\src\\main\\resources\\assets\\minefantasyreborn\\generated_recipes\\" + trimmedName + "-" + index + ".json");
					exists = tmpFile.exists();
				}
				trimmedName += "-" + index;

			}

			try (FileWriter writer = new FileWriter("C:\\git\\MineFantasy-reborn\\src\\main\\resources\\assets\\minefantasyreborn\\generated_recipes\\" + trimmedName + ".json")) {
				g.toJson(tempRecipe, writer);
			}
			catch (IOException e) {
				System.out.println("Error during writing recipe json file:");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Temporary recipe object which can be printed to json with gson
	 */
	private class TempRecipe {

		public String type;
		public String skill;
		public String research;
		public String tool_type;
		public boolean is_tool_recipe;
		public int block_tier;
		public int craft_time;
		public int recipe_hammer;
		public int experience;
		public String sound;
		public String[] pattern;
		public Map<Character, Map<String, Object>> key;
		public Map<String, Object> result;

		TempRecipe() {}


		/** Sample output
		 * {
		 *   "type": "crafting_shaped",
		 *   "skill": "none",
		 *   "research": "advblackpowder",
		 *   "tool_type": "hands",
		 *   "is_tool_recipe": false,
		 *   "block_tier": -1,
		 *   "craft_time": 10,
		 *   "recipe_hammer": -1,
		 *   "experience": 0,
		 *   "sound": "minecraft:block.wood.step",
		 *   "pattern": [
		 *     " B  ",
		 *     "RGR ",
		 *     " C  ",
		 *     "    "
		 *   ],
		 *   "key": {
		 *     "B": {
		 *       "item": "minefantasyreborn:blackpowder"
		 *     },
		 *     "R": {
		 *       "item": "minecraft:redstone"
		 *     },
		 *     "C": {
		 *       "item": "minefantasyreborn:clay_pot"
		 *     },
		 *     "G": {
		 *       "item": "minecraft:glowstone_dust"
		 *     }
		 *   },
		 *   "result": {
		 *     "item": "minefantasyreborn:blackpowder_advanced"
		 *   }
		 * }
		 */
	}

	/**
	 * Returns a random Character from a-z
	 * @return
	 */
	private static Character getRandomChar() {
		Random r = new Random();
		char c = (char) (r.nextInt(26) + 'a');
		return c;
	}

	private static String stripNamespace(String string) {
		return string.replaceAll(".+:", "");
	}
}