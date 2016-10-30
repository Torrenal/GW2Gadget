package com.torrenal.craftingGadget.ui;

import javax.swing.UnsupportedLookAndFeelException;

public class UIManager
{
	public static void launchUI()
	{
		try {
			// Set System L&F
			javax.swing.UIManager.setLookAndFeel(
					javax.swing.UIManager.getSystemLookAndFeelClassName());
		} 
		catch (UnsupportedLookAndFeelException e) {
			// handle exception
		}
		catch (ClassNotFoundException e) {
			// handle exception
		}
		catch (InstantiationException e) {
			// handle exception
		}
		catch (IllegalAccessException e) {
			// handle exception
		}
		RecipesListWindow recipeList = new RecipesListWindow();
		recipeList.initContent();

		new ItemDetailWindow(recipeList);

		LegalDialog.getConfirmation();
		
		//recipeDetail.setVisible(true);
		recipeList.setVisible(true);
//		BagsListWindow bagsList = new BagsListWindow();
//		bagsList.initContent();
//		bagsList.setVisible(true);
		
		MainWindow main = new MainWindow();
		main.setVisible(true);
	}
}
