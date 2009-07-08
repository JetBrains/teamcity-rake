Feature: Several failed steps
  In order to test Cucumber formatter
  As a great and horrible tester
  I want to check that failed features will be displayed correctly

  Scenario: group with failed steps
    When I do something wrong
    Then should fail