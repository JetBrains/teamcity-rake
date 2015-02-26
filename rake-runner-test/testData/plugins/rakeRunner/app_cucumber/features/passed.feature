Feature: Several passed steps
  In order to test Cucumber formatter
  As a great and horrible tester
  I want to check that passed features will be displayed correctly

  Scenario: first group of passed steps
    Then should pass

  Scenario: second group of passed steps
    Then should pass


  Scenario: passed group with duplicated task names
    Given nothing
    When nothing
    And nothing
    Then nothing
    Given something
    When something
    And something
    Then nothing