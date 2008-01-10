# Created by IntelliJ IDEA.
# User: Roman.Chernyatchik
# Date: 10.01.2008
# Time: 14:36:41
# To change this template use File | Settings | File Templates.
module Rake
  module TeamCity

    # Captures STDOUT and STDERR
    module StdCaptureHelper
      require 'tempfile'

      def capture_output_start
        @old_out = STDOUT.dup
        @old_err = STDERR.dup

        @new_out = Tempfile.new("tempfile_out")
        @new_err = Tempfile.new("tempfile_err")

        STDOUT.reopen(@new_out)
        STDERR.reopen(@new_err)
      end

      # returns STDOUT and STDERR content
      def capture_output_end
        STDOUT.flush
        STDERR.flush

        STDOUT.reopen(@old_out)
        STDERR.reopen(@old_err)

        @new_out.close
        @new_err.close

        begin
          @new_out.open
          s_out = @new_out.readlines.join
          @new_out.close
        rescue Exception => ex
          s_out = "Error: Teamcity agent is unable to capture STDOUT: #{ex}"
        end

        begin
          @new_err.open
          s_err = @new_err.readlines.join
          @new_err.close
        rescue Exception => ex
          s_err = "Error: Teamcity agent is unable to capture STDERR: #{ex}"
        end

        return s_out, s_err
      end
    end
  end
end
