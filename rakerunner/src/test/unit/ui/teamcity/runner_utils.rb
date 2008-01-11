# @author: Roman.Chernyatchik
# @date: 11.01.2008
# Time: 13:45:51
# To change this template use File | Settings | File Templates.
module Rake
  module TeamCity
    module RunnerUtils

      # Converts Ruby Test Names : $TEST_METHOD_NAME($TEST_CASE_NAME)
      # to TeamCity format :  $TEST_CASE_NAME.$TEST_METHOD_NAME
      def convert_ruby_test_name(ruby_name)
        if ruby_name && (ruby_name.strip =~ /(\w+)\(([\w:]*)\)/)
          # p [$1, $2]
          return  $2.empty? ? "#{$1}" : "#{$2}.#{$1}"
        end
        ruby_name
      end
    end
  end
end