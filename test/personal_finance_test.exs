defmodule PersonalFinanceTest do
  use ExUnit.Case
  doctest PersonalFinance

  test "greets the world" do
    assert PersonalFinance.hello() == :world
  end
end
