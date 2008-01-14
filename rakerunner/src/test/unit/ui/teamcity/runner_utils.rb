# @author: Roman.Chernyatchik
# @date: 11.01.2008
# Time: 13:45:51
# To change this template use File | Settings | File Templates.
module Rake
  module TeamCity
    module RunnerUtils

      # Converts Ruby Test Names : $TEST_METHOD_NAME($TEST_CASE_QUALIFIED_NAME)
      # to TeamCity format :  $NAMESPACE.$TEST_CASE_NAME.$TEST_METHOD_NAME
      def convert_ruby_test_name(ruby_name)
        if ruby_name && (ruby_name.strip =~ /(\w+)\(([\w:]*)\)/)
          # p [$1, $2]
          method_name = $1
          qualified_name = $2
          return (
                  if qualified_name.empty?
                    "#{method_name}"
                  else
                    namespace_bound = qualified_name.rindex("::")
                    "#{if namespace_bound
                         name_space = qualified_name[0, namespace_bound]
                         class_name = qualified_name[namespace_bound + 2, qualified_name.length - 1]
                         class_name && !class_name.empty? ?  "#{name_space}.#{class_name}" : qualified_name
                        else
                           qualified_name
                        end
                    }.#{method_name}"
                  end)
        end
        ruby_name
      end
    end
  end
end