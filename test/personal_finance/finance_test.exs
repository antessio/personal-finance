defmodule PersonalFinance.FinanceTest do
  use PersonalFinance.DataCase

  alias PersonalFinance.Finance

  describe "categories" do
    alias PersonalFinance.Finance.Category

    import PersonalFinance.FinanceFixtures

    @invalid_attrs %{name: nil, macro_category: nil, emoji: nil, matchers: nil}

    test "list_categories/0 returns all categories" do
      category = category_fixture()
      assert Finance.list_categories() == [category]
    end

    test "get_category!/1 returns the category with given id" do
      category = category_fixture()
      assert Finance.get_category!(category.id) == category
    end

    test "create_category/1 with valid data creates a category" do
      valid_attrs = %{name: "some name", macro_category: "some macro_category", emoji: "some emoji", matchers: ["option1", "option2"]}

      assert {:ok, %Category{} = category} = Finance.create_category(valid_attrs)
      assert category.name == "some name"
      assert category.macro_category == "some macro_category"
      assert category.emoji == "some emoji"
      assert category.matchers == ["option1", "option2"]
    end

    test "create_category/1 with invalid data returns error changeset" do
      assert {:error, %Ecto.Changeset{}} = Finance.create_category(@invalid_attrs)
    end

    test "update_category/2 with valid data updates the category" do
      category = category_fixture()
      update_attrs = %{name: "some updated name", macro_category: "some updated macro_category", emoji: "some updated emoji", matchers: ["option1"]}

      assert {:ok, %Category{} = category} = Finance.update_category(category, update_attrs)
      assert category.name == "some updated name"
      assert category.macro_category == "some updated macro_category"
      assert category.emoji == "some updated emoji"
      assert category.matchers == ["option1"]
    end

    test "update_category/2 with invalid data returns error changeset" do
      category = category_fixture()
      assert {:error, %Ecto.Changeset{}} = Finance.update_category(category, @invalid_attrs)
      assert category == Finance.get_category!(category.id)
    end

    test "delete_category/1 deletes the category" do
      category = category_fixture()
      assert {:ok, %Category{}} = Finance.delete_category(category)
      assert_raise Ecto.NoResultsError, fn -> Finance.get_category!(category.id) end
    end

    test "change_category/1 returns a category changeset" do
      category = category_fixture()
      assert %Ecto.Changeset{} = Finance.change_category(category)
    end
  end
end
