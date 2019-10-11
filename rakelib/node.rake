# frozen_string_literal: true

namespace :node do
  task atoms: %w(
    //javascript/atoms/fragments:is-displayed
    //javascript/webdriver/atoms:get-attribute
  ) do
    baseDir = 'javascript/node/selenium-webdriver/lib/atoms'
    mkdir_p baseDir

    [
      Rake::Task['//javascript/atoms/fragments:is-displayed'].out,
      Rake::Task['//javascript/webdriver/atoms:get-attribute'].out
    ].each do |atom|
      name = File.basename(atom)

      puts "Generating #{atom} as #{name}"
      File.open(File.join(baseDir, name), 'w') do |f|
        f << "// GENERATED CODE - DO NOT EDIT\n"
        f << 'module.exports = '
        f << IO.read(atom).strip
        f << ";\n"
      end
    end
  end

  task :build do
    sh 'bazel build //javascript/node/selenium-webdriver'
  end

  task 'dry-run': [
    'node:build'
  ] do
    sh 'bazel run javascript/node/selenium-webdriver:selenium-webdriver.pack'
  end

  task deploy: [
    'node:build'
  ] do
    sh 'bazel run javascript/node/selenium-webdriver:selenium-webdriver.publish'
  end

  task :docs do
    sh 'node javascript/node/gendocs.js'
  end
end
