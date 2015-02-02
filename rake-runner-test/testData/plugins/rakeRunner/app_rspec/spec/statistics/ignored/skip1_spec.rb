require "rspec"

# Only in RSpec 3
if ::RSpec::Core::Version::STRING.split('.')[0] == '3'

  describe "Skip" do

    it "should skip method" do
      skip("get sing of medved")
      fail("There is no medved here!")
    end

    it "is a skipped example without body"

    skip "is skipped" do
    end

    xit "is skipped using xit" do
    end

    xspecify "is skipped using xspecify" do
    end

    xexample "is skipped using xexample" do
    end

    example "is skipped true", :skip => true do
    end

  end

end