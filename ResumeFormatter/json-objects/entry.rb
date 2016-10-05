class Entry
  @@DELIMITER = ", "

  def self.convert(array, klass = Entry)
    return array unless array.respond_to? :map!

    array.map! {|element| klass.new element}
    array[-1].show_delimiter = false unless array[-1].nil?

    return array
  end

  def self.parse_argument(text)
    before, arg, after = text.partition(/^\s*{{\s*\d+\s*}}/)
    if arg.empty?
      arg = nil
    else
      arg = Integer(arg.slice(/(\d+)/))
    end

    template = before
    template = after if template.empty?
    [arg, template]
  end

  def self.break_into_lines(array, max_length)
    lines = []
    this_line = []
    length = 0
    array.each do |element|
      size = element.to_s().size()
      if length + size > max_length
        if this_line.empty?
          lines << [element]
        else
          lines << this_line
          this_line = [element]
          length = size
        end
      else
        this_line << element
        length += size
      end
    end
    lines << this_line unless this_line.empty?
    lines.each {|line|
      line.each {|element| element.show_delimiter = true}
      line[-1].show_delimiter = false
    }
    Entry.convert lines
  end

  def delimiter()
    @show_delimiter ? @@DELIMITER : nil
  end

  attr_accessor :value
  attr_writer  :show_delimiter
  alias_method :delim, :delimiter

  def initialize(value)
    @value = value
    @show_delimiter = true
  end

  def to_s()
    @value.nil? ? "NULL" : @value.to_s
  end
end
