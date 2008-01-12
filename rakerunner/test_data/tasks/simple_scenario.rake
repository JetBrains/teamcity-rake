namespace :simple_sc do

  #directory "./"

   #task :dist => :dist_dir do
   desc "Main task"
   task :dist do
     puts File.expand_path(".")

     user_block("File.expand_path(.)") do
       user_msg File.expand_path(".")
     end
   end
end

def user_block(name)
  puts "##[#{name}"
  yield
  puts "##]#{name}"
end

def user_msg(text)
   puts "###{text}"
end