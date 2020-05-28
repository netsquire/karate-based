Feature: Test User API
  Scenario: Fetch all users
    Given url 'https://google.com'
    When method GET
    Then status 200
#    And assert response.length == 2
#    And match response[0].name == 'FirstUser'
