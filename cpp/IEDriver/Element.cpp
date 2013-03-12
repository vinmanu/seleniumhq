// Copyright 2011 Software Freedom Conservancy
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// Ignoring code analysis warnings for:
// "'argument n' might be '0': this does not adhere to the specification for 
// the function 'IHTMLDocument4::createEventObject'", and "'argument n' might
// be null: this does not adhere to the specification for the function
// 'IHTMLDocument4::createEventObject'", and. 
// IHTMLDocument4::createEventObject() should have its first argument set to 
// NULL to create an empty event object, per documentation at:
// http://msdn.microsoft.com/en-us/library/aa752524(v=vs.85).aspx
#pragma warning (disable: 6309)
#pragma warning (disable: 6387)

#include "Element.h"
#include "Browser.h"
#include "Generated/atoms.h"
#include "interactions.h"
#include "logging.h"

namespace webdriver {

Element::Element(IHTMLElement* element, HWND containing_window_handle) {
  LOG(TRACE) << "Entering Element::Element";

  // NOTE: COM should be initialized on this thread, so we
  // could use CoCreateGuid() and StringFromGUID2() instead.
  UUID guid;
  RPC_WSTR guid_string = NULL;
  RPC_STATUS status = ::UuidCreate(&guid);
  if (status != RPC_S_OK) {
    // If we encounter an error, not bloody much we can do about it.
    // Just log it and continue.
    LOG(WARN) << "UuidCreate returned a status other then RPC_S_OK: " << status;
  }
  status = ::UuidToString(&guid, &guid_string);
  if (status != RPC_S_OK) {
    // If we encounter an error, not bloody much we can do about it.
    // Just log it and continue.
    LOG(WARN) << "UuidToString returned a status other then RPC_S_OK: " << status;
  }

  // RPC_WSTR is currently typedef'd in RpcDce.h (pulled in by rpc.h)
  // as unsigned short*. It needs to be typedef'd as wchar_t* 
  wchar_t* cast_guid_string = reinterpret_cast<wchar_t*>(guid_string);
  this->element_id_ = CW2A(cast_guid_string, CP_UTF8);

  ::RpcStringFree(&guid_string);

  this->element_ = element;
  this->containing_window_handle_ = containing_window_handle;
}

Element::~Element(void) {
}

Json::Value Element::ConvertToJson() {
  LOG(TRACE) << "Entering Element::ConvertToJson";

  Json::Value json_wrapper;
  json_wrapper["ELEMENT"] = this->element_id_;

  return json_wrapper;
}

int Element::IsDisplayed(bool* result) {
  LOG(TRACE) << "Entering Element::IsDisplayed";

  int status_code = WD_SUCCESS;

  // The atom is just the definition of an anonymous
  // function: "function() {...}"; Wrap it in another function so we can
  // invoke it with our arguments without polluting the current namespace.
  std::wstring script_source(L"(function() { return (");
  script_source += atoms::asString(atoms::IS_DISPLAYED);
  script_source += L")})();";

  CComPtr<IHTMLDocument2> doc;
  this->GetContainingDocument(false, &doc);
  // N.B., The second argument to the IsDisplayed atom is "ignoreOpacity".
  Script script_wrapper(doc, script_source, 2);
  script_wrapper.AddArgument(this->element_);
  script_wrapper.AddArgument(true);
  status_code = script_wrapper.Execute();

  if (status_code == WD_SUCCESS) {
    *result = script_wrapper.result().boolVal == VARIANT_TRUE;
  } else {
    LOG(WARN) << "Failed to determine is element displayed";
  }

  return status_code;
}

std::string Element::GetTagName() {
  LOG(TRACE) << "Entering Element::GetTagName";

  CComBSTR tag_name_bstr;
  this->element_->get_tagName(&tag_name_bstr);
  HRESULT hr = tag_name_bstr.ToLower();
  if (FAILED(hr)) {
    LOGHR(WARN, hr) << "Failed converting BSTR to lower-case with .ToLower() method";
  }
  std::string tag_name = CW2A(tag_name_bstr, CP_UTF8);
  return tag_name;
}

bool Element::IsEnabled() {
  LOG(TRACE) << "Entering Element::IsEnabled";

  bool result(false);

  // The atom is just the definition of an anonymous
  // function: "function() {...}"; Wrap it in another function so we can
  // invoke it with our arguments without polluting the current namespace.
  std::wstring script_source(L"(function() { return (");
  script_source += atoms::asString(atoms::IS_ENABLED);
  script_source += L")})();";

  CComPtr<IHTMLDocument2> doc;
  this->GetContainingDocument(false, &doc);
  Script script_wrapper(doc, script_source, 1);
  script_wrapper.AddArgument(this->element_);
  int status_code = script_wrapper.Execute();

  if (status_code == WD_SUCCESS) {
    result = script_wrapper.result().boolVal == VARIANT_TRUE;
  } else {
    LOG(WARN) << "Failed to determine is element enabled";
  }

  return result;
}

int Element::Click(const ELEMENT_SCROLL_BEHAVIOR scroll_behavior) {
  LOG(TRACE) << "Entering Element::Click";

  bool displayed;
  int status_code = this->IsDisplayed(&displayed);
  if (status_code != WD_SUCCESS) {
    LOG(WARN) << "Unable to determine element is displayed";
    return status_code;
  } 

  if (!displayed) {
    LOG(WARN) << "Element is not displayed";
    return EELEMENTNOTDISPLAYED;
  }

  LocationInfo location = {};
  status_code = this->GetLocationOnceScrolledIntoView(scroll_behavior, &location);

  if (status_code == WD_SUCCESS) {
    LocationInfo click_location = GetClickPoint(location);

    // Create a mouse move, mouse down, mouse up OS event
    LRESULT result = mouseMoveTo(this->containing_window_handle_,
                                 /* duration of move in ms = */ 10,
                                 location.x,
                                 location.y,
                                 click_location.x,
                                 click_location.y);
    if (result != WD_SUCCESS) {
      LOG(WARN) << "Unable to move mouse, mouseMoveTo returned non-zero value";
      return static_cast<int>(result);
    }
    
    result = clickAt(this->containing_window_handle_,
                     click_location.x,
                     click_location.y,
                     MOUSEBUTTON_LEFT);
    if (result != WD_SUCCESS) {
      LOG(WARN) << "Unable to click at by mouse, clickAt returned non-zero value";
      return static_cast<int>(result);
    }

    //wait(50);
  } else {
    LOG(WARN) << "Unable to get location of clicked element";
  }

  return status_code;
}

int Element::GetAttributeValue(const std::string& attribute_name,
                               std::string* attribute_value,
                               bool* value_is_null) {
  LOG(TRACE) << "Entering Element::GetAttributeValue";

  std::wstring wide_attribute_name = CA2W(attribute_name.c_str(), CP_UTF8);
  int status_code = WD_SUCCESS;

  // The atom is just the definition of an anonymous
  // function: "function() {...}"; Wrap it in another function so we can
  // invoke it with our arguments without polluting the current namespace.
  std::wstring script_source(L"(function() { return (");
  script_source += atoms::asString(atoms::GET_ATTRIBUTE);
  script_source += L")})();";

  CComPtr<IHTMLDocument2> doc;
  this->GetContainingDocument(false, &doc);
  Script script_wrapper(doc, script_source, 2);
  script_wrapper.AddArgument(this->element_);
  script_wrapper.AddArgument(wide_attribute_name);
  status_code = script_wrapper.Execute();
  
  CComVariant value_variant;
  if (status_code == WD_SUCCESS) {
    *value_is_null = !script_wrapper.ConvertResultToString(attribute_value);
  } else {
    LOG(WARN) << "Failed to determine element attribute";
  }

  return WD_SUCCESS;
}

int Element::GetLocationOnceScrolledIntoView(const ELEMENT_SCROLL_BEHAVIOR scroll,
                                             LocationInfo* location) {
  LOG(TRACE) << "Entering Element::GetLocationOnceScrolledIntoView";

  int status_code = WD_SUCCESS;
  CComPtr<IHTMLDOMNode2> node;
  HRESULT hr = this->element_->QueryInterface(&node);

  if (FAILED(hr)) {
    LOGHR(WARN, hr) << "Cannot cast html element to node, QI on IHTMLElement for IHTMLDOMNode2 failed";
    return ENOSUCHELEMENT;
  }

  LocationInfo element_location = {};
  std::vector<LocationInfo> frame_locations;
  int result = this->GetLocation(&element_location, &frame_locations);
  LocationInfo click_location = this->GetClickPoint(element_location);
  bool document_contains_frames = frame_locations.size() != 0;

  if (result != WD_SUCCESS ||
      !this->IsLocationInViewPort(click_location, document_contains_frames) ||
      this->IsHiddenByOverflow() ||
      !this->IsLocationVisibleInFrames(click_location, frame_locations)) {
    // Scroll the element into view
    LOG(DEBUG) << "Will need to scroll element into view";
    CComVariant scroll_behavior = VARIANT_TRUE;
    if (scroll == BOTTOM) {
      scroll_behavior = VARIANT_FALSE;
    }
    hr = this->element_->scrollIntoView(scroll_behavior);
    if (FAILED(hr)) {
      LOGHR(WARN, hr) << "Cannot scroll element into view, IHTMLElement::scrollIntoView failed";
      return EOBSOLETEELEMENT;
    }

    std::vector<LocationInfo> scrolled_frame_locations;
    result = this->GetLocation(&element_location, &scrolled_frame_locations);
    if (result != WD_SUCCESS) {
      LOG(WARN) << "Unable to get location of scrolled to element";
      return result;
    }

    click_location = this->GetClickPoint(element_location);
    if (!this->IsLocationInViewPort(click_location, document_contains_frames)) {
      LOG(WARN) << "Scrolled element is not in view";
      status_code = EELEMENTCLICKPOINTNOTSCROLLED;
    }
  }

  LOG(DEBUG) << "(x, y, w, h): "
             << element_location.x << ", "
             << element_location.y << ", "
             << element_location.width << ", "
             << element_location.height;

  // At this point, we know the element is displayed according to its
  // style attributes, and we've made a best effort at scrolling it so
  // that it's completely within the viewport. We will always return
  // the coordinates of the element, even if the scrolling is unsuccessful.
  // However, we will still return the "element not displayed" status code
  // if the click point has not been scrolled to the viewport.
  location->x = element_location.x;
  location->y = element_location.y;
  location->width = element_location.width;
  location->height = element_location.height;

  return status_code;
}

bool Element::IsHiddenByOverflow() {
  LOG(TRACE) << "Entering Element::IsHiddenByOverflow";

  bool isOverflow = false;

  std::wstring script_source(L"(function() { return (");
  script_source += atoms::asString(atoms::IS_IN_PARENT_OVERFLOW);
  script_source += L")})();";

  CComPtr<IHTMLDocument2> doc;
  this->GetContainingDocument(false, &doc);
  Script script_wrapper(doc, script_source, 1);
  script_wrapper.AddArgument(this->element_);
  int status_code = script_wrapper.Execute();
  if (status_code == WD_SUCCESS) {
    isOverflow = script_wrapper.result().boolVal == VARIANT_TRUE;
  } else {
    LOG(WARN) << "Unable to determine is element hidden by overflow";
  }

  return isOverflow;
}

bool Element::IsLocationVisibleInFrames(const LocationInfo location, const std::vector<LocationInfo> frame_locations) {
  std::vector<LocationInfo>::const_iterator iterator = frame_locations.begin();
  for (; iterator != frame_locations.end(); ++iterator) {
    if (location.x < iterator->x || 
        location.y < iterator->y ||
        location.x > iterator->x + iterator->width || 
        location.y > iterator->y + iterator->height) {
      return false;
    }
  }
  return true;
}

bool Element::IsSelected() {
  LOG(TRACE) << "Entering Element::IsSelected";

  bool selected(false);
  // The atom is just the definition of an anonymous
  // function: "function() {...}"; Wrap it in another function so we can
  // invoke it with our arguments without polluting the current namespace.
  std::wstring script_source(L"(function() { return (");
  script_source += atoms::asString(atoms::IS_SELECTED);
  script_source += L")})();";

  CComPtr<IHTMLDocument2> doc;
  this->GetContainingDocument(false, &doc);
  Script script_wrapper(doc, script_source, 1);
  script_wrapper.AddArgument(this->element_);
  int status_code = script_wrapper.Execute();

  if (status_code == WD_SUCCESS && script_wrapper.ResultIsBoolean()) {
    selected = script_wrapper.result().boolVal == VARIANT_TRUE;
  } else {
    LOG(WARN) << "Unable to determine is element selected";
  }

  return selected;
}

int Element::GetLocation(LocationInfo* location, std::vector<LocationInfo>* frame_locations) {
  LOG(TRACE) << "Entering Element::GetLocation";

  bool hasAbsolutePositionReadyToReturn = false;

  CComPtr<IHTMLElement2> element2;
  HRESULT hr = this->element_->QueryInterface(&element2);
  if (FAILED(hr)) {
    LOGHR(WARN, hr) << "Unable to cast element to IHTMLElement2";
    return EOBSOLETEELEMENT;
  }

  // If this element is inline, we need to check whether we should 
  // use getBoundingClientRect() or the first non-zero-sized rect returned
  // by getClientRects(). If the element is not inline, we can use
  // getBoundingClientRect() directly.
  CComPtr<IHTMLRect> rect;
  if (this->IsInline()) {
    CComPtr<IHTMLRectCollection> rects;
    hr = element2->getClientRects(&rects);
    long rect_count;
    rects->get_length(&rect_count);
    if (rect_count > 1) {
      LOG(DEBUG) << "Element is inline with multiple client rects, finding first non-zero sized client rect";
      for (long i = 0; i < rect_count; ++i) {
        CComVariant index(i);
        CComVariant rect_variant;
        hr = rects->item(&index, &rect_variant);
        if (SUCCEEDED(hr) && rect_variant.pdispVal) {
          CComPtr<IHTMLRect> qi_rect;
          rect_variant.pdispVal->QueryInterface<IHTMLRect>(&qi_rect);
          if (qi_rect) {
            rect = qi_rect;
            if (RectHasNonZeroDimensions(rect)) {
              // IE returns absolute positions in the page, rather than frame- and scroll-bound
              // positions, for clientRects (as opposed to boundingClientRects).
              hasAbsolutePositionReadyToReturn = true;
              break;
            }
          }
        }
      }
    } else {
      LOG(DEBUG) << "Element is inline with one client rect, using IHTMLElement2::getBoundingClientRect";
      hr = element2->getBoundingClientRect(&rect);
    }
  } else {
    LOG(DEBUG) << "Element is a block element, using IHTMLElement2::getBoundingClientRect";
    hr = element2->getBoundingClientRect(&rect);
  }
  if (FAILED(hr)) {
    LOGHR(WARN, hr) << "Cannot figure out where the element is on screen, client rect retrieval failed";
    return EUNHANDLEDERROR;
  }

  // If the rect of the element has zero width and height, check its
  // children to see if any of them have width and height, in which
  // case, this element will be visible.
  if (!RectHasNonZeroDimensions(rect)) {
    LOG(DEBUG) << "Element has client rect with zero dimension, checking children for non-zero dimension client rects";
    CComPtr<IHTMLDOMNode> node;
    element2->QueryInterface(&node);
    CComPtr<IDispatch> children_dispatch;
    node->get_childNodes(&children_dispatch);
    CComPtr<IHTMLDOMChildrenCollection> children;
    children_dispatch->QueryInterface<IHTMLDOMChildrenCollection>(&children);
    if (!!children) {
      long childrenCount = 0;
      children->get_length(&childrenCount);
      for (long i = 0; i < childrenCount; ++i) {
        CComPtr<IDispatch> childDispatch;
        children->item(i, &childDispatch);
        CComPtr<IHTMLElement> child;
        childDispatch->QueryInterface(&child);
        if (child != NULL) {
          Element childElement(child, this->containing_window_handle_);
          std::vector<LocationInfo> child_frame_locations;
          int result = childElement.GetLocation(location, &child_frame_locations);
          if (result == WD_SUCCESS) {
            return result;
          }
        }
      }
    }
  }

  long top = 0, bottom = 0, left = 0, right = 0;

  rect->get_top(&top);
  rect->get_left(&left);
  rect->get_bottom(&bottom);
  rect->get_right(&right);

  long w = right - left;
  long h = bottom - top;

  if (!hasAbsolutePositionReadyToReturn) {
    // On versions of IE prior to 8 on Vista, if the element is out of the 
    // viewport this would seem to return 0,0,0,0. IE 8 returns position in 
    // the DOM regardless of whether it's in the browser viewport.
    long scroll_left, scroll_top = 0;
    element2->get_scrollLeft(&scroll_left);
    element2->get_scrollTop(&scroll_top);
    left += scroll_left;
    top += scroll_top;

    // Only add the frame offset if the element is actually in a frame.
    LocationInfo frame_location = {};
    bool element_is_in_frame = this->GetFrameDetails(&frame_location, frame_locations);
    if (element_is_in_frame) {
      left += frame_location.x;
      top += frame_location.y;
      frame_locations->push_back(frame_location);
    } else {
      LOG(DEBUG) << "Element is not in a frame";
    }
  }

  location->x = left;
  location->y = top;
  location->width = w;
  location->height = h;

  return WD_SUCCESS;
}

bool Element::IsInline() {
  LOG(TRACE) << "Entering Element::IsInline";

  // TODO(jimevans): Clean up this extreme lameness.
  // We should be checking styles here for whether the
  // element is inline or not.
  CComPtr<IHTMLAnchorElement> anchor;
  HRESULT hr = this->element_->QueryInterface(&anchor);
  if (anchor) {
    return true;
  }

  CComPtr<IHTMLSpanElement> span;
  hr = this->element_->QueryInterface(&span);
  if (span) {
    return true;
  }

  return false;
}

bool Element::RectHasNonZeroDimensions(const CComPtr<IHTMLRect> rect) {
  LOG(TRACE) << "Entering Element::RectHasNonZeroDimensions";

  long top = 0, bottom = 0, left = 0, right = 0;

  rect->get_top(&top);
  rect->get_left(&left);
  rect->get_bottom(&bottom);
  rect->get_right(&right);

  long w = right - left;
  long h = bottom - top;

  return w > 0 && h > 0;
}

bool Element::GetFrameDetails(LocationInfo* location, std::vector<LocationInfo>* frame_locations) {
  LOG(TRACE) << "Entering Element::GetFrameDetails";

  CComPtr<IHTMLDocument2> owner_doc;
  int status_code = this->GetContainingDocument(true, &owner_doc);
  if (status_code != WD_SUCCESS) {
    LOG(WARN) << "Unable to get containing document";
    return false;
  }

  CComPtr<IHTMLWindow2> owner_doc_window;
  HRESULT hr = owner_doc->get_parentWindow(&owner_doc_window);
  if (!owner_doc_window) {
    LOG(WARN) << "Unable to get parent window, call to IHTMLDocument2::get_parentWindow failed";
    return false;
  }

  // Get the parent window to the current window, where "current window" is
  // the window containing the parent document of this element. If that parent
  // window exists, and it is not the same as the current window, we assume
  // this element exists inside a frame or iframe. If it is in a frame, get
  // the parent document containing the frame, so we can get the information
  // about the frame or iframe element hosting the document of this element.
  CComPtr<IHTMLWindow2> parent_window;
  hr = owner_doc_window->get_parent(&parent_window);
  if (parent_window && !owner_doc_window.IsEqualObject(parent_window)) {
    LOG(DEBUG) << "Element is in a frame.";
    CComPtr<IHTMLDocument2> parent_doc;
    status_code = this->GetDocumentFromWindow(parent_window, &parent_doc);

    CComPtr<IHTMLFramesCollection2> frames;
    hr = parent_doc->get_frames(&frames);

    long frame_count(0);
    hr = frames->get_length(&frame_count);
    CComVariant index;
    index.vt = VT_I4;
    for (long i = 0; i < frame_count; ++i) {
      // See if the document in each frame is this element's 
      // owner document.
      index.lVal = i;
      CComVariant result;
      hr = frames->item(&index, &result);
      CComPtr<IHTMLWindow2> frame_window;
      result.pdispVal->QueryInterface<IHTMLWindow2>(&frame_window);
      if (!frame_window) {
        // Frame is not an HTML frame.
        continue;
      }

      CComPtr<IHTMLDocument2> frame_doc;
      status_code = this->GetDocumentFromWindow(frame_window, &frame_doc);

      if (frame_doc.IsEqualObject(owner_doc)) {
        // The document in this frame *is* this element's owner
        // document. Get the frameElement property of the document's
        // containing window (which is itself an HTML element, either
        // a frame or an iframe). Then get the x and y coordinates of
        // that frame element.
        // N.B. We must use JavaScript here, as directly using
        // IHTMLWindow4.get_frameElement() returns E_NOINTERFACE under
        // some circumstances.
        LOG(DEBUG) << "Located host frame. Attempting to get hosting element";
        std::wstring script_source = L"(function(){ return function() { return arguments[0].frameElement };})();";
        Script script_wrapper(frame_doc, script_source, 1);
        CComVariant window_variant(frame_window);
        script_wrapper.AddArgument(window_variant);
        status_code = script_wrapper.Execute();
        CComPtr<IHTMLFrameBase> frame_base;
        if (status_code == WD_SUCCESS) {
          hr = script_wrapper.result().pdispVal->QueryInterface<IHTMLFrameBase>(&frame_base);
          if (FAILED(hr)) {
            LOG(WARN) << "Found the frame element, but could not QueryInterface to IHTMLFrameBase.";
          }
        } else {
          // Can't get the frameElement property, likely because the frames are from different
          // domains. So start at the parent document, and use getElementsByTagName to retrieve
          // all of the iframe elements (if there are no iframe elements, get the frame elements)
          // **** BIG HUGE ASSUMPTION!!! ****
          // The index of the frame from the document.frames collection will correspond to the 
          // index into the collection of iframe/frame elements returned by getElementsByTagName.
          LOG(WARN) << "Attempting to get frameElement via JavaScript failed. "
                    << "This usually means the frame is in a different domain than the parent frame. "
                    << "Browser security against cross-site scripting attacks will not allow this. "
                    << "Attempting alternative method.";
          long collection_count = 0;
          CComPtr<IDispatch> element_dispatch;
          CComPtr<IHTMLDocument3> doc;
          parent_doc->QueryInterface<IHTMLDocument3>(&doc);
          if (doc) {
            LOG(DEBUG) << "Looking for <iframe> elements in parent document.";
            BSTR iframe_tag_name = L"iframe";
            CComPtr<IHTMLElementCollection> iframe_collection;
            hr = doc->getElementsByTagName(iframe_tag_name, &iframe_collection);
            hr = iframe_collection->get_length(&collection_count);
            if (collection_count != 0) {
              if (collection_count > index.lVal) {
                LOG(DEBUG) << "Found <iframe> elements in parent document, retrieving element" << index.lVal << ".";
                hr = iframe_collection->item(index, index, &element_dispatch);
                hr = element_dispatch->QueryInterface<IHTMLFrameBase>(&frame_base);
              }
            } else {
              LOG(DEBUG) << "No <iframe> elements, looking for <frame> elements in parent document.";
              BSTR frame_tag_name = L"iframe";
              CComPtr<IHTMLElementCollection> frame_collection;
              hr = doc->getElementsByTagName(frame_tag_name, &frame_collection);
              hr = frame_collection->get_length(&collection_count);
              if (collection_count > index.lVal) {
                LOG(DEBUG) << "Found <frame> elements in parent document, retrieving element" << index.lVal << ".";
                hr = frame_collection->item(index, index, &element_dispatch);
                hr = element_dispatch->QueryInterface<IHTMLFrameBase>(&frame_base);
              }
            }
          } else {
            LOG(WARN) << "QueryInterface of parent document to IHTMLDocument3 failed.";
          }
        }

        if (frame_base) {
          LOG(DEBUG) << "Successfully found frame hosting element";
          LocationInfo frame_doc_info;
          bool doc_dimensions_success = DocumentHost::GetDocumentDimensions(
              frame_doc,
              &frame_doc_info);

          // Wrap the element so we can find its location. Note that
          // GetLocation() may recursively call into this method.
          CComPtr<IHTMLElement> frame_element;
          frame_base->QueryInterface<IHTMLElement>(&frame_element);
          Element element_wrapper(frame_element, this->containing_window_handle_);
          CComPtr<IHTMLStyle> style;
          frame_element->get_style(&style);

          LocationInfo frame_location = {};
          status_code = element_wrapper.GetLocation(&frame_location,
                                                    frame_locations);

          // Take the border of the frame element into account.
          // N.B. We don't have to do this for non-frame elements,
          // because the border is part of the hit-test region. For
          // finding offsets to get absolute position of elements 
          // within frames, the origin of the frame document is offset
          // by the border width.
          CComPtr<IHTMLElement2> border_width_element;
          frame_element->QueryInterface<IHTMLElement2>(&border_width_element);
          long left_border_width = 0;
          border_width_element->get_clientLeft(&left_border_width);
          long top_border_width = 0;
          border_width_element->get_clientTop(&top_border_width);

          if (status_code == WD_SUCCESS) {
            // Take into account the presence of scrollbars in the frame.
            long frame_element_width = frame_location.width;
            long frame_element_height = frame_location.height;
            if (doc_dimensions_success) {
              if (frame_doc_info.height > frame_element_height) {
                int horizontal_scrollbar_height = ::GetSystemMetrics(SM_CYHSCROLL);
                frame_element_height -= horizontal_scrollbar_height;
              }
              if (frame_doc_info.width > frame_element_width) {
                int vertical_scrollbar_width = ::GetSystemMetrics(SM_CXVSCROLL);
                frame_element_width -= vertical_scrollbar_width;
              }
            }
            location->x = frame_location.x + left_border_width;
            location->y = frame_location.y + top_border_width;
            location->width = frame_element_width;
            location->height = frame_element_height;
          }
          return true;
        }
      }
    }
  }

  // If we reach here, the element isn't in a frame/iframe.
  return false;
}

LocationInfo Element::GetClickPoint(const LocationInfo location) {
  LOG(TRACE) << "Entering Element::GetClickPoint";

  LocationInfo click_location = {};
  //Note: This logic is duplicated in javascript in Element::IsHiddenByOverflow
  click_location.x = location.x + (location.width / 2);
  click_location.y = location.y + (location.height / 2);
  return click_location;
}

bool Element::IsLocationInViewPort(const LocationInfo location, const bool document_contains_frames) {
  LOG(TRACE) << "Entering Element::IsLocationInViewPort";

  WINDOWINFO window_info;
  window_info.cbSize = sizeof(WINDOWINFO);
  BOOL get_window_info_result = ::GetWindowInfo(this->containing_window_handle_, &window_info);
  if (get_window_info_result == FALSE) {
    DWORD error_code = ::GetLastError();
    LOG(WARN) << "Cannot determine size of window, call to GetWindowInfo API failed with error code " << error_code;
    return false;
  }

  long window_width = window_info.rcClient.right - window_info.rcClient.left;
  long window_height = window_info.rcClient.bottom - window_info.rcClient.top;

  if (!document_contains_frames) {
    // ASSUMPTION! IE **always** draws a vertical scroll bar, even if it's not
    // required. This means the viewport width is always smaller than the window
    // width by at least the width of the vertical scroll bar.
    int vertical_scrollbar_width = ::GetSystemMetrics(SM_CXVSCROLL);
    window_width -= vertical_scrollbar_width;

    // Horizontal scrollbar will only appear if the document is wider than the
    // viewport.
    CComPtr<IHTMLDocument2> doc;
    this->GetContainingDocument(false, &doc);
    LocationInfo document_info;
    DocumentHost::GetDocumentDimensions(doc, &document_info);
    if (document_info.width > window_width) {
      int horizontal_scrollbar_height = ::GetSystemMetrics(SM_CYHSCROLL);
      window_height -= horizontal_scrollbar_height;
    }
  }

  // Hurrah! Now we know what the visible area of the viewport is
  // Is the element visible in the X axis?
  // N.B. There is an n-pixel sized area next to the client area border
  // where clicks are interpreted as a click on the window border, not
  // within the client area. We are assuming n == 2, but that's strictly
  // a wild guess, not based on any research.
  if (location.x < 0 || location.x >= window_width - 2) {
    LOG(WARN) << "X coordinate is out of element area";
    return false;
  }

  // And in the Y?
  if (location.y < 0 || location.y >= window_height - 2) {
    LOG(WARN) << "Y coordinate is out of element area";
    return false;
  }

  return true;
}

int Element::GetContainingDocument(const bool use_dom_node,
                                   IHTMLDocument2** doc) {
  LOG(TRACE) << "Entering Element::GetContainingDocument";

  HRESULT hr = S_OK;
  CComPtr<IDispatch> dispatch_doc;

  if (use_dom_node) {
    CComPtr<IHTMLDOMNode2> node;
    hr = this->element_->QueryInterface(&node);
    if (FAILED(hr)) {
      LOGHR(WARN, hr) << "Unable to cast element to IHTMLDomNode2";
      return ENOSUCHDOCUMENT;
    }

    hr = node->get_ownerDocument(&dispatch_doc);
    if (FAILED(hr)) {
      LOGHR(WARN, hr) << "Unable to locate owning document, call to IHTMLDOMNode2::get_ownerDocument failed";
      return ENOSUCHDOCUMENT;
    }
  } else {
    hr = this->element_->get_document(&dispatch_doc);
    if (FAILED(hr)) {
      LOGHR(WARN, hr) << "Unable to locate document property, call to IHTMLELement::get_document failed";
      return ENOSUCHDOCUMENT;
    }

  }

  try {
    hr = dispatch_doc.QueryInterface<IHTMLDocument2>(doc);
    if (FAILED(hr)) {
      LOGHR(WARN, hr) << "Found document but it's not the expected type (IHTMLDocument2)";
      return ENOSUCHDOCUMENT;
    }
  } catch(...) {
    LOG(WARN) << "Found document but it's not the expected type (IHTMLDocument2)";
    return ENOSUCHDOCUMENT;
  }

  return WD_SUCCESS;
}

int Element::GetDocumentFromWindow(IHTMLWindow2* parent_window,
                                   IHTMLDocument2** parent_doc) {
  LOG(TRACE) << "Entering Element::GetParentDocument";

  HRESULT hr = parent_window->get_document(parent_doc);
  if (FAILED(hr)) {
    if (hr == E_ACCESSDENIED) {
      // Cross-domain documents may throw Access Denied. If so,
      // get the document through the IWebBrowser2 interface.
      CComPtr<IServiceProvider> service_provider;
      hr = parent_window->QueryInterface<IServiceProvider>(&service_provider);
      if (FAILED(hr)) {
        LOGHR(WARN, hr) << "Unable to get browser, call to IHTMLWindow2::QueryInterface failed for IServiceProvider";
        return ENOSUCHDOCUMENT;
      }
      CComPtr<IWebBrowser2> window_browser;
      hr = service_provider->QueryService(IID_IWebBrowserApp, &window_browser);
      if (FAILED(hr)) {
        LOGHR(WARN, hr) << "Unable to get browser, call to IServiceProvider::QueryService failed for IID_IWebBrowserApp";
        return ENOSUCHDOCUMENT;
      }
      CComPtr<IDispatch> parent_doc_dispatch;
      hr = window_browser->get_Document(&parent_doc_dispatch);
      if (FAILED(hr)) {
        LOGHR(WARN, hr) << "Unable to get document, call to IWebBrowser2::get_Document failed";
        return ENOSUCHDOCUMENT;
      }
      try {
        hr = parent_doc_dispatch->QueryInterface<IHTMLDocument2>(parent_doc);
        if (FAILED(hr)) {
          LOGHR(WARN, hr) << "Unable to get document, QueryInterface for IHTMLDocument2 failed";
          return ENOSUCHDOCUMENT;
        }
      } catch(...) {
        LOG(WARN) << "Unable to get document, exception thrown attempting to QueryInterface for IHTMLDocument2";
        return ENOSUCHDOCUMENT;
      }
    } else {
      LOGHR(WARN, hr) << "Unable to get document, IHTMLWindow2::get_document failed with error code other than E_ACCESSDENIED";
      return ENOSUCHDOCUMENT;
    }
  }
  return WD_SUCCESS;
}

int Element::ExecuteAsyncAtom(const std::wstring& sync_event_name, ASYNCEXECPROC execute_proc, std::string* error_msg) {
    CComPtr<IHTMLDocument2> doc;
    this->GetContainingDocument(false, &doc);

    // Marshal the document and the element to click to streams for use in another thread.
    LOG(DEBUG) << "Marshaling document to stream to send to new thread";
    LPSTREAM document_stream;
    HRESULT hr = ::CoMarshalInterThreadInterfaceInStream(IID_IHTMLDocument2, doc, &document_stream);
    if (FAILED(hr)) {
      LOGHR(WARN, hr) << "CoMarshalInterfaceThreadInStream() for document failed";
      *error_msg = "Couldn't marshal the IHTMLDocument2 interface to a stream. This is an internal COM error.";
      return EUNEXPECTEDJSERROR;
    }

    // We need exclusive access to this event. If it's already created,
    // OpenEvent returns non-NULL, so we need to wait a bit and retry
    // until OpenEvent returns NULL.
    int retry_counter = 50;
    HANDLE event_handle = ::OpenEvent(SYNCHRONIZE, FALSE, sync_event_name.c_str());
    if (event_handle != NULL && --retry_counter > 0) {
      ::CloseHandle(event_handle);
      ::Sleep(50);
      event_handle = ::OpenEvent(SYNCHRONIZE, FALSE, sync_event_name.c_str());
    }

    // Failure condition here.
    if (event_handle != NULL) {
      ::CloseHandle(event_handle);
      LOG(WARN) << "OpenEvent() returned non-NULL, event already exists.";
      *error_msg = "Couldn't create an event for synchronizing the creation of the thread. This generally means that you were trying to click on an option in two different instances.";
      return EUNEXPECTEDJSERROR;
    }

    LOG(DEBUG) << "Creating synchronization event for new thread";
    event_handle = ::CreateEvent(NULL, TRUE, FALSE, sync_event_name.c_str());
    if (event_handle == NULL) {
      LOG(WARN) << "CreateEvent() failed.";
      *error_msg = "Couldn't create an event for synchronizing the creation of the thread. This is an internal failure at the Windows OS level, and is generally not due to an error in the IE driver.";
      return EUNEXPECTEDJSERROR;
    }

    // Start the thread and wait up to 1 second to be signaled that it is ready
    // to receive messages, then close the event handle.
    LOG(DEBUG) << "Starting new thread";
    unsigned int thread_id = 0;
    HANDLE thread_handle = reinterpret_cast<HANDLE>(_beginthreadex(NULL,
                                                    0,
                                                    execute_proc,
                                                    reinterpret_cast<void*>(document_stream),
                                                    0,
                                                    &thread_id));
    LOG(DEBUG) << "Waiting for new thread to be ready for messages";
    DWORD event_wait_result = ::WaitForSingleObject(event_handle, 5000);
    if (event_wait_result != WAIT_OBJECT_0) {
      LOG(WARN) << "Waiting for event to be signaled returned unexpected value: " << event_wait_result;
    }
    ::CloseHandle(event_handle);

    if (thread_handle == NULL) {
      LOG(WARN) << "_beginthreadex() failed.";
      *error_msg = "Couldn't create the thread for executing JavaScript asynchronously.";
      return EUNEXPECTEDJSERROR;
    }

    // This is why we shouldn't do this all the time. We have no way to
    // verify the success or failure of the called function, so we have to
    // assume we just succeeded.
    LOG(DEBUG) << "Marshaling element to stream to send to thread";
    int status_code = WD_SUCCESS;
    LPSTREAM element_stream;
    hr = ::CoMarshalInterThreadInterfaceInStream(IID_IDispatch, this->element_, &element_stream);
    if (FAILED(hr)) {
      LOGHR(WARN, hr) << "CoMarshalInterfaceThreadInStream() for element failed";
      *error_msg = "Couldn't marshal the IHTMLElement interface to a stream. This is an internal COM error.";
      status_code = EUNEXPECTEDJSERROR;
    } else {
      // Post the message to execute the atom to the worker thread.
      // Try to let the thread complete within a short amount of time
      // to have a hope of synchronization.
      LOG(DEBUG) << "Posting thread message";
      DWORD post_message_result = ::PostThreadMessage(thread_id, WD_EXECUTE_ASYNC_SCRIPT, NULL, reinterpret_cast<LPARAM>(element_stream));
      DWORD wait_result = ::WaitForSingleObject(thread_handle, 100);
      if (wait_result == WAIT_OBJECT_0) {
        LOG(DEBUG) << "Thread exited successfully";
      } else if (wait_result == WAIT_TIMEOUT) {
        LOG(DEBUG) << "Thread still running. This does not necesarily mean an error condition. There may be a valid alert displayed.";
      } else {
        LOG(WARN) << "WaitForSingleObject returned an unexpected value: " << wait_result;
      }
    }
    ::CloseHandle(thread_handle);
    return status_code;
}

bool Element::IsAttachedToDom() {
  // Verify that the element is still valid by walking up the
  // DOM tree until we find no parent or the html tag
  if (this->element_) {
    CComPtr<IHTMLDOMNode2> node;
    HRESULT hr = this->element_->QueryInterface(&node);
    if (FAILED(hr)) {
      LOGHR(WARN, hr) << "Unable to cast element to IHTMLDomNode2";
      return false;
    }

    CComPtr<IDispatch> dispatch_doc;
    hr = node->get_ownerDocument(&dispatch_doc);
    if (FAILED(hr)) {
      LOGHR(WARN, hr) << "Unable to locate owning document, call to IHTMLDOMNode2::get_ownerDocument failed";
      return false;
    }

    if (dispatch_doc) {
      CComPtr<IHTMLDocument3> doc;
      hr = dispatch_doc.QueryInterface<IHTMLDocument3>(&doc);
      if (FAILED(hr)) {
        LOGHR(WARN, hr) << "Found document but it's not the expected type (IHTMLDocument3)";
        return false;
      }

      CComPtr<IHTMLElement> document_element;
      hr = doc->get_documentElement(&document_element);
      if (FAILED(hr)) {
        LOGHR(WARN, hr) << "Unable to locate document element, call to IHTMLDocument3::get_documentElement failed";
        return false;
      }

      if (document_element) {
        VARIANT_BOOL contains(VARIANT_FALSE);
        hr = document_element->contains(this->element_, &contains);
        if (FAILED(hr)) {
          LOGHR(WARN, hr) << "Call to IHTMLElement::contains failed";
          return false;
        }

        return contains == VARIANT_TRUE;
      }
    }
  }
  return false;
}

bool Element::HasOnlySingleTextNodeChild() {
  CComPtr<IHTMLDOMNode> element_node;
  HRESULT hr = this->element_.QueryInterface<IHTMLDOMNode>(&element_node);
  if (FAILED(hr)) {
    LOGHR(WARN, hr) << "QueryInterface for IHTMLDOMNode on element failed.";
    return false;
  }

  CComPtr<IDispatch> child_nodes_dispatch;
  hr = element_node->get_childNodes(&child_nodes_dispatch);
  if (FAILED(hr)) {
    LOGHR(WARN, hr) << "Call to get_childNodes on element failed.";
    return false;
  }

  CComPtr<IHTMLDOMChildrenCollection> child_nodes;
  hr = child_nodes_dispatch.QueryInterface<IHTMLDOMChildrenCollection>(&child_nodes);

  long length = 0;
  hr = child_nodes->get_length(&length);
  if (FAILED(hr)) {
    LOGHR(WARN, hr) << "Call to get_length on child nodes collection failed.";
    return false;
  }

  if (length > 1) {
    CComPtr<IDispatch> child_dispatch;
    hr = child_nodes->item(0, &child_dispatch);
    if (FAILED(hr)) {
      LOGHR(WARN, hr) << "Call to item(0) on child nodes collection failed.";
      return false;
    }

    CComPtr<IHTMLDOMNode> child_node;
    hr = child_dispatch.QueryInterface<IHTMLDOMNode>(&child_node);
    if (FAILED(hr)) {
      LOGHR(WARN, hr) << "QueryInterface for IHTMLDOMNode on child node failed.";
      return false;
    }

    long node_type = 0;
    hr = child_node->get_nodeType(&node_type);
    if (FAILED(hr)) {
      LOGHR(WARN, hr) << "Call to get_nodeType on child node failed.";
      return false;
    }

    if (node_type == 3) {
      return true;
    }
  }
  return false;
}

bool Element::GetTextBoundaries(LocationInfo* text_info) {
  CComPtr<IHTMLDocument2> doc;
  this->GetContainingDocument(false, &doc);
  CComPtr<IHTMLElement> body_element;
  HRESULT hr = doc->get_body(&body_element);
  if (FAILED(hr)) {
    LOGHR(WARN, hr) << "Call to get_body on document failed.";
    return false;
  }

  CComPtr<IHTMLBodyElement> body;
  hr = body_element.QueryInterface<IHTMLBodyElement>(&body);
  if (FAILED(hr)) {
    LOGHR(WARN, hr) << "QueryInterface for IHTMLBodyElement on body element failed.";
    return false;
  }

  CComPtr<IHTMLTxtRange> range;
  hr = body->createTextRange(&range);
  if (FAILED(hr)) {
    LOGHR(WARN, hr) << "Call to createTextRange on body failed.";
    return false;
  }

  hr = range->moveToElementText(this->element_);
  if (FAILED(hr)) {
    LOGHR(WARN, hr) << "Call to moveToElementText on range failed.";
    return false;
  }

  CComPtr<IHTMLTextRangeMetrics> range_metrics;
  hr = range.QueryInterface<IHTMLTextRangeMetrics>(&range_metrics);
  if (FAILED(hr)) {
    LOGHR(WARN, hr) << "QueryInterface for IHTMLTextRangeMetrics on range failed.";
    return false;
  }

  long height = 0;
  hr = range_metrics->get_boundingHeight(&height);
  if (FAILED(hr)) {
    LOGHR(WARN, hr) << "Call to get_boundingHeight on range metrics failed.";
    return false;
  }

  long width = 0;
  hr = range_metrics->get_boundingWidth(&width);
  if (FAILED(hr)) {
    LOGHR(WARN, hr) << "Call to get_boundingWidth on range metrics failed.";
    return false;
  }

  text_info->height = height;
  text_info->width = width;
  return true;
}

} // namespace webdriver