require_relative 'resume-writer'

##
# Helper class to format and filter resume data before
# applying a template and writing it.
class ResumeFormatter

  ##
  # Factory
  # ----
  # Exists to make reading the code easier
  #
  # See #initialize
  def self.fromSource(data)
    return ResumeFormatter.new(data)
  end

  ##
  # Convert to a writer
  # ----
  # This is a terminal operation, after which #reject will no longer be accessible.
  #
  # The data gets filtered and passed to a writer for processing/saving
  def formatWith(template_file)
    return ResumeWriter.new template_file, filteredData()
  end

  ##
  # = Add a filter
  # ----
  # The filter added here will not be immediately applied, but deferred to just
  # before the conversion to a writer
  #
  # Calling multiple times appends filters instead of replacing them. They are
  # applied as an +or+ operation, so if any filter returns true the target contents
  # will not be included.
  #
  # == Parameters
  # +section_name+:: +String+ The name of the top-level key in the data to which this
  #                           filter will be applied.
  # +filter+:: +Proc+ The filter operation, should return true to reject the contents
  #                   or false to select the contents instead. If this is omitted, then
  #                   the section will be unconditionally rejected.
  #
  # == Example
  # === Sample JSON data
  #     {"title": "Sample Data",
  #      "sub-title": "Has no real purpose",
  #      "hash0" : {"key0": 0, "key1": 1, "key2": 2},
  #      "hash1" : {"key0": 0, "key1": 1, "key2": 2},
  #      "array": [1,2,3]
  #     }
  # === Code
  #     ResumeFormatter.fromSource(JSON.parse(sample_json_data))
  #                    .reject('sub-title') {true}
  #                    .reject('hash0') {true}
  #                    .reject('hash1') {|key,value| value == 1}
  #                    .reject('hash1') {|key,value| key == "key0"}
  #                    .reject('array') {|value| value >= 2}
  # === Output
  # The data that will be sent to the mustache template:
  #     {"title": "Sample Data",
  #      "hash1" : {"key2": 2},
  #      "array": [1]
  #     }
  def reject(section_name, &filter)
    @section_filters[section_name] << (filter || Proc.new{true})
    return self
  end

  ##
  # Constructor
  # ----
  # +data+:: +Hash+ of values to pass into the mustache template
  def initialize(data)
    @data = data
    @section_filters = Hash.new {|h,k| h[k] = []}
  end

  ##
  # Filters the data and adds dynamically generated markup
  # ----
  # This applies the filters added in #reject to the direct contents of each section. If
  # a section's contents are completely filtered it will be removed entirely.
  #
  # Most of the work is delegated to #applyFilters
  def filteredData()
    data = Hash[
      @data.map {|section, contents| applyFilters([section, contents])}
    ].reject {|_,contents| contents.nil? or contents.empty?}

    return ResumeFormatter.markFirstAndLast data
  end

  private

  ##
  # Filters the data in a section
  # ----
  # Parameter:
  # +section+:: +Array+ with two elements <code>[section_name, section_contents]</code>
  #
  # Returns:
  # +Array+ with two elements <code>[section_name, filtered_contents]</code>
  #
  # +section_name+:: The same value as in the +section+ parameter
  # +filtered_contents+:: The result of all of the filter operations, may be +nil+ or just empty
  def applyFilters(section)
    section_name, section_contents = section
    unless @section_filters.key? section_name
      return [section_name, section_contents]
    end

    contents = section_contents
    @section_filters[section_name].each do |filter|
      return [section_name, nil] if contents.nil?

      if contents.respond_to? :reject
        contents = contents.reject(&filter.method(:call))
      else
        contents = filter.call ? nil : contents
      end

    end
    return [section_name, contents]
  end

  ##
  # Adds first/last markups to rows
  # ----
  # Adds marker values to rows to make templating with mustache easier.
  #
  # Rows are defined as an +Array+ of +Hash+ with the single key +row+.
  #
  # Example:
  #     [{'row' => 0},{'row' => 1},{'row' => 2}]
  #     # => [{'row' => 0, 'first' => true},{'row' => 1},{'row' => 2, 'last' => true}]
  #
  # The values of the +first+ and +last+ keys are not important, as mustache is only
  # really interested in presence when the <code>{{^first}}stuff{{/first}}</code> idiom
  # is used.
  #
  # Sample data:
  #     {'names' => [{'row' => 'Tom',   'first' => 1},
  #                  {'row' => 'Dick'},
  #                  {'row' => 'Harry', 'last' => 1}]}
  #
  # Sample template:
  #     {{#names}}{{#last}}and {{/last}}{{row}}{{^last}}, {{/last}}{{/names}}
  #
  # Output:
  #     Tom, Dick, and Harry
  def self.markFirstAndLast(obj)
    if obj.is_a?(Array)
      first, last = 0, obj.size - 1
      obj = obj.each_with_index.map do |e,i|
        if e.is_a?(Hash) and e.key?('row')
          e['first'] = true if i == first
          e['last']  = true if i == last
        else
          # need to recurse
          e = self.markFirstAndLast e
        end
        e
      end

    elsif obj.is_a?(Hash)
      obj = Hash[obj.map {|k,v| [k, self.markFirstAndLast(v)]}]
    end

    return obj
  end
end
