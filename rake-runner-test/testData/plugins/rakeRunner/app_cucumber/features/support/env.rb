require 'rubygems'

class CucumberWorld

end

World do
  CucumberWorld.new
end

require "rspec"

RSpec.configure do |config|
  # Disable color output
  config.color = false
end