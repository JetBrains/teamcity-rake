Feature: Several steps with background
  In order to test Cucumber formatter
  As a great and horrible tester
  I want to check that background steps will be displayed correctly

  Background:
    Given background

  Scenario: first group with several steps
    Then should pass

  Scenario: second group with several steps
    Then should pass        